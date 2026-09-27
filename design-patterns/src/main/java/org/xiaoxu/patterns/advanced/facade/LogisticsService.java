package org.xiaoxu.patterns.advanced.facade;

/** 子系统：物流服务 */
public class LogisticsService {

    public void arrangeShipment(String orderNo, String address) {
        System.out.println("[物流] 单号=" + orderNo + " 安排发往 " + address);
    }
}
