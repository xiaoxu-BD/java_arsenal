package org.xiaoxu.pay.vo;

import lombok.Data;

/**
 * 支付结果 VO
 */
@Data
public class PayResultVO {

    /** 付款单号 */
    private String orderNo;

    /** 支付表单 HTML（用于跳转支付宝） */
    private String payForm;

    /** 支付链接（H5支付） */
    private String payUrl;
}
