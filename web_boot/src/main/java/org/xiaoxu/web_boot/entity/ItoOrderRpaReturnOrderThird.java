package org.xiaoxu.web_boot.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 第三方退货订单清单列表
 * ito_order_rpa_return_order_third
 */
@Data
public class ItoOrderRpaReturnOrderThird implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 公司代码
     */
    private String ouc;

    /**
     * 客户号
     */
    private String customerNo;

    /**
     * 客户订单号
     */
    private String orderSn;

    /**
     * 付款方id
     */
    private String invoicePayerId;

    /**
     * 系统订单号
     */
    private String cohOrderId;

    /**
     * 系统行号
     */
    private String cohLineNo;

    /**
     * 系统分行号
     */
    private String cohSort;

    /**
     * 型号
     */
    private String model;

    /**
     * 接受的交货数量
     */
    private Integer acceptedDeliveryQuantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 订单总净价
     */
    private BigDecimal totalNetPrice;

    /**
     * 开发票日期
     */
    private Date invoiceDate;

    /**
     * 产地
     */
    private String originPlace;

    /**
     * 系统发表号
     */
    private String invoiceNo;

    /**
     * 系统发票行号
     */
    private String invoiceLine;

    /**
     * 第三方系统待退货数量(COH/SAP)
     */
    private Integer returnQuantity;

    /**
     * 版本号(每一次修改，增加版本号)
     */
    private Integer version;

    /**
     * 创建者(创建人员)
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新者(更新人员)
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 逻辑删除
     */
    private String delFlag;

    private String remarks;

    /**
     * 包装代码
     */
    private String packageCode;

    /**
     * 实际还有多少可退数量
     */
    private Integer realReturnQuantity;

    /**
     * 增值税发票号
     */
    private String valueAddedTax;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 爸爸系统订单号
     */
    private String parentCohOrderId;

    /**
     * 爸爸系统行号
     */
    private String parentCohLineNo;

    /**
     * 爸爸系统分行号
     */
    private String parentCohSort;

    /**
     * 顶层系统订单号
     */
    private String topCohOrderId;

    /**
     * 顶层系统行号
     */
    private String topCohLineNo;

    /**
     * 顶层系统分行号
     */
    private String topCohSort;

    /**
     * 爸爸发票号
     */
    private String parentInvoiceNo;

    /**
     * 爸爸发票行号
     */
    private String parentInvoiceLine;

    /**
     * 父类的数据来源 coh  sap
     */
    private String parentDataSource;

    /**
     * 箱单号
     */
    private String packingListNo;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 数据来源 coh sap
     */
    private String dataSource;

    @Serial
    private static final long serialVersionUID = 1L;
}