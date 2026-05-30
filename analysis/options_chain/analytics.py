"""Core analytics engine: volatility smile, skew, term structure, Greeks surface."""

import math
from dataclasses import dataclass, field
from typing import Optional

from .data_types import OptionChain, OptionQuote, OptionType


# ---------------------------------------------------------------------------
#  Smile / Skew
# ---------------------------------------------------------------------------

@dataclass
class SmilePoint:
    """A single point on the volatility smile curve."""
    strike: float
    log_moneyness: float        # ln(K / S)
    implied_volatility: float
    delta: float
    option_type: OptionType
    quote: OptionQuote


@dataclass
class VolatilitySmile:
    """Volatility smile for a single expiry slice."""
    expiry_label: str           # e.g., "2026-06-19 (30d)"
    spot: float
    atm_strike: float
    atm_iv: float
    points: list[SmilePoint] = field(default_factory=list)

    # Skew metrics
    put_call_skew: float = 0.0          # 25D put IV - 25D call IV
    butterfly_25d: float = 0.0          # (25D put + 25D call) / 2 - ATM IV
    risk_reversal_25d: float = 0.0      # 25D call IV - 25D put IV

    @property
    def max_iv(self) -> float:
        return max((p.implied_volatility for p in self.points), default=0.0)

    @property
    def min_iv(self) -> float:
        return min((p.implied_volatility for p in self.points), default=0.0)


# ---------------------------------------------------------------------------
#  Term Structure
# ---------------------------------------------------------------------------

@dataclass
class TermPoint:
    """A single point on the IV term structure curve."""
    expiry_label: str
    days_to_expiry: int
    atm_iv: float


@dataclass
class TermStructure:
    """IV term structure: ATM IV across multiple expiries."""
    underlying_symbol: str
    spot: float
    points: list[TermPoint] = field(default_factory=list)

    @property
    def is_backwardation(self) -> Optional[bool]:
        """Short-dated IV > long-dated IV (downward sloping)."""
        if len(self.points) < 2:
            return None
        return self.points[0].atm_iv > self.points[-1].atm_iv

    @property
    def is_contango(self) -> Optional[bool]:
        """Short-dated IV < long-dated IV (upward sloping)."""
        if len(self.points) < 2:
            return None
        return self.points[0].atm_iv < self.points[-1].atm_iv


# ---------------------------------------------------------------------------
#  Greeks Surface
# ---------------------------------------------------------------------------

@dataclass
class GreeksSurfacePoint:
    """Greeks at a specific strike/expiry coordinate."""
    strike: float
    expiry_label: str
    days_to_expiry: int
    log_moneyness: float
    delta: float
    gamma: float
    theta: float
    vega: float


@dataclass
class GreeksSurface:
    """Full Greeks surface across strikes and expiries."""
    underlying_symbol: str
    spot: float
    points: list[GreeksSurfacePoint] = field(default_factory=list)

    def filter_by_delta_range(
        self, lo: float = -1.0, hi: float = 1.0
    ) -> list[GreeksSurfacePoint]:
        return [p for p in self.points if lo <= p.delta <= hi]

    def filter_by_expiry(self, expiry_label: str) -> list[GreeksSurfacePoint]:
        return [p for p in self.points if p.expiry_label == expiry_label]


# ---------------------------------------------------------------------------
#  OI / Volume Profile
# ---------------------------------------------------------------------------

@dataclass
class OiProfile:
    """Open interest profile at a single strike."""
    strike: float
    call_oi: int
    put_oi: int
    call_volume: int
    put_volume: int
    total_oi: int
    oi_ratio: float             # put_oi / call_oi (or 0 if call_oi is 0)


@dataclass
class OiChain:
    """OI & volume distribution across the entire option chain."""
    underlying_symbol: str
    spot: float
    expiry_label: str
    profiles: list[OiProfile] = field(default_factory=list)

    @property
    def max_pain_strike(self) -> Optional[float]:
        """
        Max-pain strike: the price at which option writers
        (sellers) suffer the least total loss.

        Calculated by assuming all options expire at each
        candidate strike and summing the total intrinsic
        payout to option holders; the strike with the
        minimum payout is max pain.
        """
        if not self.profiles:
            return None

        strikes = [p.strike for p in self.profiles]
        oi_by_strike: dict[float, tuple[int, int]] = {}
        for p in self.profiles:
            oi_by_strike[p.strike] = (p.call_oi, p.put_oi)

        min_loss = math.inf
        max_pain = strikes[0]

        for candidate in strikes:
            total_loss = 0.0
            for s in strikes:
                call_oi, put_oi = oi_by_strike[s]
                # Call holders profit if candidate > s
                total_loss += max(candidate - s, 0.0) * call_oi
                # Put holders profit if candidate < s
                total_loss += max(s - candidate, 0.0) * put_oi

            if total_loss < min_loss:
                min_loss = total_loss
                max_pain = candidate

        return max_pain

    @property
    def total_call_oi(self) -> int:
        return sum(p.call_oi for p in self.profiles)

    @property
    def total_put_oi(self) -> int:
        return sum(p.put_oi for p in self.profiles)

    @property
    def put_call_oi_ratio(self) -> float:
        call = self.total_call_oi
        if call == 0:
            return 0.0
        return self.total_put_oi / call


# ---------------------------------------------------------------------------
#  Factory helpers
# ---------------------------------------------------------------------------

def build_smile(chain: OptionChain) -> VolatilitySmile:
    """Build a volatility smile from a single-expiry option chain."""
    atm = chain.atm_strike or 0.0
    spot = chain.underlying_price

    # Find ATM IV
    atm_iv = 0.0
    for c in chain.calls:
        if c.strike_price == atm:
            atm_iv = c.implied_volatility
            break

    expiry_label = chain.expiry_date.strftime("%Y-%m-%d")

    points: list[SmilePoint] = []
    for q in chain.calls + chain.puts:
        log_m = math.log(q.strike_price / spot) if spot > 0 else 0.0
        points.append(SmilePoint(
            strike=q.strike_price,
            log_moneyness=log_m,
            implied_volatility=q.implied_volatility,
            delta=q.greeks.delta,
            option_type=q.option_type,
            quote=q,
        ))

    # Sort by strike
    points.sort(key=lambda p: p.strike)

    smile = VolatilitySmile(
        expiry_label=expiry_label,
        spot=spot,
        atm_strike=atm,
        atm_iv=atm_iv,
        points=points,
    )

    # Compute 25-delta skew (approximate from points)
    # Find points closest to delta = 0.25 and delta = -0.25
    call_25d = min(
        (p for p in points if p.option_type == OptionType.CALL),
        key=lambda p: abs(p.delta - 0.25),
        default=None,
    )
    put_25d = min(
        (p for p in points if p.option_type == OptionType.PUT),
        key=lambda p: abs(p.delta - (-0.25)),
        default=None,
    )
    if call_25d and put_25d:
        smile.risk_reversal_25d = call_25d.implied_volatility - put_25d.implied_volatility
        smile.put_call_skew = put_25d.implied_volatility - call_25d.implied_volatility
        smile.butterfly_25d = (
            (put_25d.implied_volatility + call_25d.implied_volatility) / 2 - atm_iv
        )

    return smile


def build_oi_chain(chain: OptionChain) -> OiChain:
    """Build OI profile from a single-expiry option chain."""
    spot = chain.underlying_price
    expiry_label = chain.expiry_date.strftime("%Y-%m-%d")

    # Merge calls and puts by strike
    by_strike: dict[float, dict] = {}

    for c in chain.calls:
        by_strike.setdefault(c.strike_price, {})
        by_strike[c.strike_price]["call_oi"] = c.open_interest
        by_strike[c.strike_price]["call_volume"] = c.volume

    for p in chain.puts:
        by_strike.setdefault(p.strike_price, {})
        by_strike[p.strike_price]["put_oi"] = p.open_interest
        by_strike[p.strike_price]["put_volume"] = p.volume

    profiles: list[OiProfile] = []
    for strike in sorted(by_strike.keys()):
        d = by_strike[strike]
        call_oi = d.get("call_oi", 0)
        put_oi = d.get("put_oi", 0)
        profiles.append(OiProfile(
            strike=strike,
            call_oi=call_oi,
            put_oi=put_oi,
            call_volume=d.get("call_volume", 0),
            put_volume=d.get("put_volume", 0),
            total_oi=call_oi + put_oi,
            oi_ratio=put_oi / call_oi if call_oi > 0 else 0.0,
        ))

    return OiChain(
        underlying_symbol=chain.underlying_symbol,
        spot=spot,
        expiry_label=expiry_label,
        profiles=profiles,
    )
