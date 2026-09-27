package org.xiaoxu.patterns.basic.chain;

/** 具体处理者：总监，最多批 7 天 */
public class Director extends Approver {

    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 7) {
            System.out.println("总监批准了 " + request.getName() + " 请假 " + request.getDays() + " 天");
        } else {
            System.out.println("总监：请假 " + request.getDays() + " 天？直接辞退吧（超过任何人的审批权限）");
        }
    }
}
