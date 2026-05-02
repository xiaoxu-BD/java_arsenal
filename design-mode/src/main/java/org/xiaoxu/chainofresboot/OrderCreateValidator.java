package org.xiaoxu.chainofresboot;

import org.xiaoxu.chainofresboot.request.OrderCreateRequest;

public interface OrderCreateValidator {


    void setNext(OrderCreateValidator nextValidator);


    OrderCreateValidator getNext();



    void validate(OrderCreateRequest createRequest);
}
