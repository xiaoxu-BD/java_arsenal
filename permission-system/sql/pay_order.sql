
create table pay_order(

    id  bigint comment '支付中心主键',

    pay_order_no varchar(32) comment '支付中心订单号',

    identifier varchar(32) comment '米登号',

    amount decimal (19,2) comment '订单金额',

    business_order_no varchar(32) comment '业务订单号',

    status  varchar(32)  comment '支付单状态 CREATE WAIT_PAY PAID '
)