package org.xiaoxu.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xiaoxu.channel.domain.ChannelOperationLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChannelLogMapper extends BaseMapper<ChannelOperationLog> {

    @Select("SELECT * FROM channel_operation_log WHERE idempotent_key = #{idempotentKey} LIMIT 1")
    ChannelOperationLog selectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

    @Select("SELECT * FROM channel_operation_log WHERE channel_code = #{channelCode} AND biz_id = #{bizId} LIMIT 1")
    ChannelOperationLog selectByBiz(@Param("channelCode") String channelCode, @Param("bizId") String bizId);

    @Select("SELECT * FROM channel_operation_log WHERE channel_code = #{channelCode} AND callback_seq_no = #{seqNo} LIMIT 1")
    ChannelOperationLog selectByCallbackSeqNo(@Param("channelCode") String channelCode, @Param("seqNo") String seqNo);

    /**
     * 补偿扫描：捞取待重试流水
     *
     * ⚠ <script> 标签触发 MyBatis XML 解析器
     *    内部的 < > 必须转义为 &lt; &gt;
     *    否则报：SAXParseException: 元素内容必须由格式正确的字符数据或标记组成
     *
     * FOR UPDATE SKIP LOCKED：集群部署时跳过已锁定行，避免重复消费
     */
    @Select({
        "<script>",
        "SELECT * FROM channel_operation_log",
        "WHERE status IN ('PROCESSING', 'TIMEOUT')",
        "  AND next_retry_at &lt;= #{now}",
        "  AND retry_count &lt; max_retry",
        "ORDER BY next_retry_at ASC",
        "LIMIT #{limit}",
        "FOR UPDATE SKIP LOCKED",
        "</script>"
    })
    List<ChannelOperationLog> selectRetryable(@Param("now") LocalDateTime now, @Param("limit") int limit);

    @Update({
        "UPDATE channel_operation_log SET",
        "status = #{status},",
        "retry_count = #{retryCount},",
        "next_retry_at = #{nextRetryAt},",
        "response_body = COALESCE(#{responseBody}, response_body),",
        "callback_body = COALESCE(#{callbackBody}, callback_body),",
        "callback_seq_no = COALESCE(#{callbackSeqNo}, callback_seq_no),",
        "error_msg = COALESCE(#{errorMsg}, error_msg),",
        "finished_at = COALESCE(#{finishedAt}, finished_at)",
        "WHERE id = #{id}"
    })
    int updateStatus(ChannelOperationLog log);

    /**
     * 硬超时：将超过5分钟仍处于PROCESSING的流水强制置TIMEOUT
     *
     * ⚠ 这里没有 <script> 标签，所以 < 直接写即可，不需要转义
     */
    @Update({
        "UPDATE channel_operation_log SET status = 'TIMEOUT', error_msg = '硬超时: 流水挂起超过5分钟'",
        "WHERE status = 'PROCESSING'",
        "  AND created_at < #{threshold}",
        "  AND retry_count < max_retry"
    })
    int forceTimeoutStuck(@Param("threshold") LocalDateTime threshold);
}
