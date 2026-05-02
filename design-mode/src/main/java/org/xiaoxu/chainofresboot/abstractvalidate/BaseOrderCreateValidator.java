package org.xiaoxu.chainofresboot.abstractvalidate;

import org.xiaoxu.chainofresboot.OrderCreateValidator;
import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

public abstract class BaseOrderCreateValidator implements OrderCreateValidator {

    protected OrderCreateValidator nextValidator;

    @Override
    public void setNext(OrderCreateValidator nextValidator) {
        this.nextValidator = nextValidator;
    }

    @Override
    public OrderCreateValidator getNext() {
        return nextValidator;
    }

    @Override
    public void validate(OrderCreateRequest createRequest) {

        // 先执行自己的校验
        doValidate(createRequest);


        // 再交给下一个校验器
        if (nextValidator !=null){
            nextValidator.validate(createRequest);
        }
    }



    protected abstract void doValidate(OrderCreateRequest request);
}
