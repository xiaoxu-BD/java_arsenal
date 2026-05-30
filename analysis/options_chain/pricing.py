"""Black-Scholes pricing model for European options."""

import math
from typing import Optional

from .data_types import Greeks, OptionType


def _norm_cdf(x: float) -> float:
    """Standard normal cumulative distribution function."""
    return 0.5 * (1.0 + math.erf(x / math.sqrt(2.0)))


def _norm_pdf(x: float) -> float:
    """Standard normal probability density function."""
    return math.exp(-0.5 * x * x) / math.sqrt(2.0 * math.pi)


def black_scholes_price(
    option_type: OptionType,
    spot: float,
    strike: float,
    time_to_expiry: float,  # in years
    risk_free_rate: float,
    volatility: float,
    dividend_yield: float = 0.0,
) -> float:
    """
    Black-Scholes pricing formula for European options.

    Parameters
    ----------
    option_type : OptionType
        CALL or PUT.
    spot : float
        Current price of the underlying.
    strike : float
        Strike price.
    time_to_expiry : float
        Time to expiration in years (e.g., 30/365).
    risk_free_rate : float
        Annualized risk-free interest rate (e.g., 0.05 for 5%).
    volatility : float
        Annualized implied volatility (e.g., 0.20 for 20%).
    dividend_yield : float, optional
        Continuous dividend yield.

    Returns
    -------
    float
        Theoretical option price.
    """
    if time_to_expiry <= 0:
        # At or past expiry: payoff
        if option_type == OptionType.CALL:
            return max(spot - strike, 0.0)
        else:
            return max(strike - spot, 0.0)

    if volatility <= 0:
        raise ValueError("volatility must be positive for non-expired options")

    d1 = (
        math.log(spot / strike)
        + (risk_free_rate - dividend_yield + 0.5 * volatility ** 2) * time_to_expiry
    ) / (volatility * math.sqrt(time_to_expiry))

    d2 = d1 - volatility * math.sqrt(time_to_expiry)

    if option_type == OptionType.CALL:
        price = spot * math.exp(-dividend_yield * time_to_expiry) * _norm_cdf(d1) \
                - strike * math.exp(-risk_free_rate * time_to_expiry) * _norm_cdf(d2)
    else:
        price = strike * math.exp(-risk_free_rate * time_to_expiry) * _norm_cdf(-d2) \
                - spot * math.exp(-dividend_yield * time_to_expiry) * _norm_cdf(-d1)

    return price


def black_scholes_greeks(
    option_type: OptionType,
    spot: float,
    strike: float,
    time_to_expiry: float,
    risk_free_rate: float,
    volatility: float,
    dividend_yield: float = 0.0,
) -> Greeks:
    """
    Compute Greeks via Black-Scholes analytical formulas.

    Returns
    -------
    Greeks
        Dataclass containing delta, gamma, theta, vega, rho.

    Notes
    -----
    - Theta is returned as per-calendar-day (annual / 365).
    - Vega is per 1% change in IV (i.e., divided by 100).
    - Rho is per 1% change in rate (i.e., divided by 100).
    """
    if time_to_expiry <= 0:
        return Greeks()

    sqrt_t = math.sqrt(time_to_expiry)
    d1 = (
        math.log(spot / strike)
        + (risk_free_rate - dividend_yield + 0.5 * volatility ** 2) * time_to_expiry
    ) / (volatility * sqrt_t)

    d2 = d1 - volatility * sqrt_t
    exp_qt = math.exp(-dividend_yield * time_to_expiry)
    exp_rt = math.exp(-risk_free_rate * time_to_expiry)
    pdf_d1 = _norm_pdf(d1)

    # Delta
    if option_type == OptionType.CALL:
        delta = exp_qt * _norm_cdf(d1)
    else:
        delta = -exp_qt * _norm_cdf(-d1)

    # Gamma (same for call and put)
    gamma = (exp_qt * pdf_d1) / (spot * volatility * sqrt_t)

    # Theta (per day)
    common_theta = -(spot * pdf_d1 * volatility * exp_qt) / (2 * sqrt_t)
    if option_type == OptionType.CALL:
        theta_annual = common_theta \
            - risk_free_rate * strike * exp_rt * _norm_cdf(d2) \
            + dividend_yield * spot * exp_qt * _norm_cdf(d1)
    else:
        theta_annual = common_theta \
            + risk_free_rate * strike * exp_rt * _norm_cdf(-d2) \
            - dividend_yield * spot * exp_qt * _norm_cdf(-d1)
    theta = theta_annual / 365.0

    # Vega (per 1% IV)
    vega = (spot * exp_qt * sqrt_t * pdf_d1) / 100.0

    # Rho (per 1% rate)
    if option_type == OptionType.CALL:
        rho = (strike * time_to_expiry * exp_rt * _norm_cdf(d2)) / 100.0
    else:
        rho = -(strike * time_to_expiry * exp_rt * _norm_cdf(-d2)) / 100.0

    return Greeks(
        delta=delta,
        gamma=gamma,
        theta=theta,
        vega=vega,
        rho=rho,
    )


def implied_volatility(
    option_type: OptionType,
    spot: float,
    strike: float,
    time_to_expiry: float,
    risk_free_rate: float,
    market_price: float,
    dividend_yield: float = 0.0,
    precision: float = 1e-6,
    max_iterations: int = 200,
) -> Optional[float]:
    """
    Newton-Raphson solver for implied volatility.

    Returns
    -------
    float or None
        Implied volatility, or None if it fails to converge.
    """
    if time_to_expiry <= 0:
        return None

    # Initial guess
    iv = math.sqrt(2 * math.pi / time_to_expiry) * market_price / spot
    iv = max(iv, 0.01)

    for _ in range(max_iterations):
        try:
            price = black_scholes_price(
                option_type, spot, strike, time_to_expiry,
                risk_free_rate, iv, dividend_yield,
            )
        except ValueError:
            return None

        vega_raw = (
            spot
            * math.exp(-dividend_yield * time_to_expiry)
            * math.sqrt(time_to_expiry)
            * _norm_pdf(
                (math.log(spot / strike)
                 + (risk_free_rate - dividend_yield + 0.5 * iv ** 2) * time_to_expiry)
                / (iv * math.sqrt(time_to_expiry))
            )
        )

        diff = market_price - price
        if abs(diff) < precision:
            return iv

        if vega_raw < 1e-12:
            return None

        iv += diff / vega_raw
        iv = max(iv, 1e-4)

    return None
