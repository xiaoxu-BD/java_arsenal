package org.xiaoxu.patterns.basic.state;

/** 具体状态：已完成（终态，任何操作都不再允许） */
public class CompletedState implements OrderState {

    @Override
    public String name() {
        return "已完成";
    }
    // 不覆盖任何 default 方法 -> 所有操作统一被拒绝
}
