package org.xiaoxu.chainofresboot.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoxu.chainofresboot.abstractvalidate.BaseOrderCreateValidator;
import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

public class UserValidator extends BaseOrderCreateValidator {

    //这里我们可以注入一些具有查询能力的业务类比如： userService 让他帮助我们实现校验
    private static final Logger LOGGER = LoggerFactory.getLogger(UserValidator.class);

    @Override
    protected void doValidate(OrderCreateRequest request) {

        LOGGER.info("执行USER Validator 逻辑");

        if (request.getUserId() < 0) {
            throw  new RuntimeException("用户ID不合法");
        }
    }



//    当然如果使用外部类，我们一般进行持有外部类的构造
//    public UserValidator(UserService  userService){
//        this.userSerivce = userService;
//    }
}
