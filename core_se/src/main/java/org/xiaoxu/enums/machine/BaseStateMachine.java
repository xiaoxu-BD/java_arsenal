package org.xiaoxu.enums.machine;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.util.Map;

public class BaseStateMachine<STATE,EVENT>  implements StateMachine<STATE,EVENT> {

    private final Map<String,STATE> stateTransitions = Maps.newHashMap();


    protected  void putTransition(STATE origin, EVENT event, STATE target){

        stateTransitions.put(Joiner.on("-").join(origin,event),target);
    }

    @Override
    public STATE transition(STATE state, EVENT event) {

        STATE target = stateTransitions.get(Joiner.on("-").join(state, event));
        if (target == null){
            throw new IllegalArgumentException("状态不合法,不允许转换");
        }
        return target;
    }
}
