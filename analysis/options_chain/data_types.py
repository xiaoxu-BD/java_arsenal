"""Option chain data structures for TUIST."""

from dataclasses import dataclass, field
from datetime import date, datetime
from enum import Enum
from typing import Optional


class OptionType(Enum):
    """期权类型"""
    CALL = "CALL"
    PUT = "PUT"


class OptionStyle(Enum):
    """期权行权风格"""
    EUROPEAN = "EUROPEAN"
    AMERICAN = "AMERICAN"


class ExerciseStatus(Enum):
    """行权状态"""
    ITM = "ITM"          # In The Money - 实值
    ATM = "ATM"          # At The Money - 平值
    OTM = "OTM"          # Out of The Money - 虚值
    DEEP_ITM = "DEEP_ITM"
    DEEP_OTM = "DEEP_OTM"


@dataclass
class Greeks:
    """期权希腊字母"""
    delta: float = 0.0
    gamma: float = 0.0
    theta: float = 0.0
    vega: float = 0.0
    rho: float = 0.0


@dataclass
class OptionQuote:
    """单个期权合约报价"""
    symbol: str                          # 合约代码
    option_type: OptionType              # CALL / PUT
    strike_price: float                  # 行权价
    expiry_date: date                    # 到期日
    style: OptionStyle = OptionStyle.EUROPEAN

    # 行情数据
    last_price: float = 0.0
    bid_price: float = 0.0
    ask_price: float = 0.0
    bid_size: int = 0
    ask_size: int = 0
    volume: int = 0
    open_interest: int = 0
    implied_volatility: float = 0.0

    # 希腊值
    greeks: Greeks = field(default_factory=Greeks)

    # 内在/时间价值
    intrinsic_value: float = 0.0
    time_value: float = 0.0

    # 状态
    status: ExerciseStatus = ExerciseStatus.OTM
    days_to_expiry: int = 0

    # 时间戳
    timestamp: Optional[datetime] = None

    @property
    def mid_price(self) -> float:
        """中间价"""
        if self.bid_price > 0 and self.ask_price > 0:
            return (self.bid_price + self.ask_price) / 2
        return self.last_price

    @property
    def spread(self) -> float:
        """买卖价差"""
        if self.bid_price > 0 and self.ask_price > 0:
            return self.ask_price - self.bid_price
        return 0.0

    @property
    def spread_pct(self) -> float:
        """买卖价差百分比"""
        if self.mid_price > 0:
            return self.spread / self.mid_price * 100
        return 0.0


@dataclass
class OptionChain:
    """期权链 - 同一标的、同一到期日的所有期权"""
    underlying_symbol: str               # 标的代码
    underlying_price: float              # 标的现价
    expiry_date: date                    # 到期日
    risk_free_rate: float = 0.0          # 无风险利率
    dividend_yield: float = 0.0          # 股息率

    calls: list[OptionQuote] = field(default_factory=list)
    puts: list[OptionQuote] = field(default_factory=list)

    timestamp: Optional[datetime] = None

    @property
    def strike_prices(self) -> list[float]:
        """获取所有行权价（去重排序）"""
        strikes = set()
        for c in self.calls:
            strikes.add(c.strike_price)
        for p in self.puts:
            strikes.add(p.strike_price)
        return sorted(strikes)

    @property
    def atm_strike(self) -> Optional[float]:
        """平值行权价（最接近标的价格）"""
        strikes = self.strike_prices
        if not strikes:
            return None
        return min(strikes, key=lambda s: abs(s - self.underlying_price))

    def get_call(self, strike: float) -> Optional[OptionQuote]:
        """按行权价获取看涨期权"""
        for c in self.calls:
            if c.strike_price == strike:
                return c
        return None

    def get_put(self, strike: float) -> Optional[OptionQuote]:
        """按行权价获取看跌期权"""
        for p in self.puts:
            if p.strike_price == strike:
                return p
        return None

    def get_pair(self, strike: float) -> tuple[Optional[OptionQuote], Optional[OptionQuote]]:
        """获取同一行权价的 call/put 对"""
        return self.get_call(strike), self.get_put(strike)
