package org.xiaoxu.service;

public interface SignService {

    /**
     * 持久化签到记录：扫描Redis Bitmap当月所有签到数据，写入 t_sign_in 表
     */
    void persistSignIns();
}
