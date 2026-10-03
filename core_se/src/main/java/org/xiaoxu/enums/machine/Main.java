package org.xiaoxu.enums.machine;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.enums.TradeOrderEvent;
import org.xiaoxu.enums.TradeOrderState;

@Slf4j
public class Main {


    public static void main(String[] args) {

        OrderStateMachine orderStateMachine = new OrderStateMachine();


        TradeOrderState transition = orderStateMachine.transition(TradeOrderState.CREATE, TradeOrderEvent.DISCARD);


        log.info("transaction: {}",transition);

    }
}
