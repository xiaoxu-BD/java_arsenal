package org.xiaoxu.service;

import org.xiaoxu.domain.dto.CallbackPayload;
import org.xiaoxu.domain.dto.PaymentRequest;
import org.xiaoxu.domain.dto.PaymentResponse;

public interface PaymentService {


    PaymentResponse pay(PaymentRequest request);




    void handleCallback(CallbackPayload payload);




    PaymentResponse queryStatus(String orderId);
}