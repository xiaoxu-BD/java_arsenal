package org.xiaoxu.wecommail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 业务配置项(对应 application.yml 中 wecom.*)
 */
@Data
@ConfigurationProperties(prefix = "wecom")
public class WecomMailProperties {

    /**
     * 其他开发组群列表 HTTP 接入(接入节点一:配置 roomId 时下拉数据源)。
     * url 未配置时,新增群功能不可用(接口返回业务错误)。
     */
    private final RoomListApi roomListApi = new RoomListApi();

    private final Mail mail = new Mail();

    @Data
    public static class RoomListApi {
        /** 群列表接口地址,如 http://other-team/api/rooms */
        private String url = "";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;
        /** 同步结果缓存 TTL(分钟):TTL 内直接读本地库,过期后重新拉上游 */
        private long cacheTtlMinutes = 5;
    }

    @Data
    public static class Mail {
        private final Send send = new Send();

        @Data
        public static class Send {
            /** 定时扫描 cron,默认每分钟 */
            private String scheduleCron = "0 * * * * *";
            /** SENDING 卡死判定阈值(分钟),超过则强制置 FAIL 允许重试 */
            private int sendingTimeoutMinutes = 10;
            /** 失败自动重试上限 */
            private int retryMaxCount = 3;
            /** 各次重试退避间隔(分钟),按 retryCount 取对应下标 */
            private List<Integer> retryBackoffMinutes = List.of(5, 15, 30);
            /**
             * 迟到消息补充轮延迟(分钟):当日已发过 SUCCESS 后,又来新消息,
             * 距上一轮发送超过该延迟才发补充轮(0=来了就发)。用于攒批,避免每条迟到消息一封邮件。
             */
            private int lateResendDelayMinutes = 10;
        }
    }
}
