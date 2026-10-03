package org.xiaoxu.enums.machine;

import org.xiaoxu.enums.TradeOrderEvent;
import org.xiaoxu.enums.TradeOrderState;

public class OrderStateMachine extends BaseStateMachine<TradeOrderState,TradeOrderEvent> {



    public OrderStateMachine(){
        putTransition(TradeOrderState.CREATE,TradeOrderEvent.CONFIRM,TradeOrderState.CONFIRM);
        putTransition(TradeOrderState.CREATE, TradeOrderEvent.DISCARD, TradeOrderState.DISCARD);
    }

}
