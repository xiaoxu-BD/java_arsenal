package org.xiaoxu.chainofresboot.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoxu.chainofresboot.abstractvalidate.BaseOrderCreateValidator;
import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

public class GoodsValidator extends BaseOrderCreateValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsValidator.class);
    @Override
    protected void doValidate(OrderCreateRequest request) {
        LOGGER.info("开始商品校验");
        if (request.getGoodsId() < 0 ){
//            模拟商品不存在
            throw new RuntimeException("商品不存在");
        }
    }
}
