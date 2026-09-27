package org.xiaoxu.patterns.basic.chain;

/** 请假单：沿审批链传递的请求载体 */
public class LeaveRequest {

    private final String name;
    private final int days;

    public LeaveRequest(String name, int days) {
        this.name = name;
        this.days = days;
    }

    public String getName() { return name; }
    public int getDays() { return days; }
}
