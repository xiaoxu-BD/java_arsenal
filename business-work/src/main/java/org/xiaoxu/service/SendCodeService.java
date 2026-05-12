package org.xiaoxu.service;

public interface SendCodeService {

    /**
     * 异步发送验证码：模拟厂商接口，约5秒后返回4位数字
     */
    void asyncSendCode(String phone);
}
