package org.xiaoxu.patterns.basic.chain;

/** 具体处理者：经理，能批 3 天以内 */
public class Manager extends Approver {

    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 3) {
            System.out.println("经理批准了 " + request.getName() + " 请假 " + request.getDays() + " 天");
        } else if (next != null) {
            System.out.println("经理：超过 3 天，我批不了，转交上级 ->");
            next.handle(request);
        } else {
            System.out.println("无人能审批 " + request.getName() + " 的请假申请");
        }
    }
}
