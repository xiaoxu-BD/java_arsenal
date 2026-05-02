package org.xiaoxu.chainofresboot.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoxu.chainofresboot.abstractvalidate.BaseOrderCreateValidator;
import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

public class StockValidator extends BaseOrderCreateValidator {


    private static final Logger LOGGER = LoggerFactory.getLogger(StockValidator.class);

    @Override
    protected void doValidate(OrderCreateRequest request) {
        LOGGER.info("执行库存校验逻辑");
        if (request.getCount() < 0){
         throw  new RuntimeException("库存不足");
        }
    }
}
