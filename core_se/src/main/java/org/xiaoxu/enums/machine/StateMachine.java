package org.xiaoxu.enums.machine;


//泛型（Generics）只能使用引用类型
public interface StateMachine<STATE, EVENT> {

    /**
     * 状态机转移
     *
     * @param state 引用类型 必须
     * @param event 引用类型 必须
     * @return  引用类型 必须
     */
    public STATE transition(STATE state, EVENT event);
}
