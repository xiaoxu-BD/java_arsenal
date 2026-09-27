package org.xiaoxu.patterns.basic.chain;

/** 具体处理者：组长，只能批 1 天以内 */
public class GroupLeader extends Approver {

    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 1) {
            System.out.println("组长批准了 " + request.getName() + " 请假 " + request.getDays() + " 天");
        } else if (next != null) {
            System.out.println("组长：超过 1 天，我批不了，转交上级 ->");
            next.handle(request);
        } else {
            System.out.println("无人能审批 " + request.getName() + " 的请假申请");
        }
    }
}
