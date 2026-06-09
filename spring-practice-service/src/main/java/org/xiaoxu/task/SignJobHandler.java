package org.xiaoxu.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.service.SignService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignJobHandler {

    private final SignService signService;

    /**
     * XXL-Job handler：持久化签到记录
     * 从Redis Bitmap读取今日签到数据，同步写入 t_sign_in 表
     */
    @XxlJob("persistSignInsHandler")
    public void persistSignIns() {
        log.info("XXL-Job: 开始持久化签到记录...");
        signService.persistSignIns();
        log.info("XXL-Job: 签到记录持久化完成");
    }
}
