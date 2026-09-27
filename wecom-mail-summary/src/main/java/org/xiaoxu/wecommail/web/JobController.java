package org.xiaoxu.wecommail.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.common.ApiResponse;
import org.xiaoxu.wecommail.scheduler.MailSendScheduler;

/**
 * 运维辅助接口:手动触发定时扫描(免等整分钟,触发建任务/认领/重试/超时兜底)。
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final MailSendScheduler scheduler;

    @PostMapping("/scan")
    public ApiResponse<String> triggerScan() {
        Thread worker = new Thread(scheduler::scan, "manual-scan");
        worker.setDaemon(true);
        worker.start();
        return ApiResponse.ok("扫描已触发(异步),稍后可通过 GET /api/send-records 查看结果");
    }
}
