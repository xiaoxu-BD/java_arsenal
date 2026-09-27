package org.xiaoxu.wecommail.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.xiaoxu.wecommail.entity.CargoDeclareInfo;
import org.xiaoxu.wecommail.entity.EnterpriseWechatRecord;
import org.xiaoxu.wecommail.mapper.CargoDeclareInfoMapper;
import org.xiaoxu.wecommail.mapper.EnterpriseWechatRecordMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MQ 消费逻辑(消息产品无关,后续接 RocketMQ/RabbitMQ 时 Listener 只需回调 {@link #onMessage})。
 *
 * <p>校验链:JSON 合法 → msgtype=text → roomid 非空 → msgid 非空 → 群已配置(valid=1) → 幂等入库。
 * 所有业务性忽略分支均视为消费成功(不 requeue);幂等以 t_e_enterprise_wechat_record.uk_msg_id
 * 唯一约束为最终裁决,不依赖 Redis。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMessageConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final EnterpriseWechatRecordMapper recordMapper;
    private final CargoDeclareInfoMapper configMapper;

    /** 消费结果,联调入口(/api/messages)与未来的 MQ Listener 会返回/记录它,便于观察各分支 */
    public enum ConsumeResult {
        CONSUMED("已入库"),
        DUPLICATED("重复消息,幂等忽略"),
        IGNORED_NOT_TEXT("非 text 类型,忽略"),
        IGNORED_NO_ROOMID("roomid 为空(非群消息),忽略"),
        IGNORED_ROOM_NOT_CONFIGURED("群未配置,忽略"),
        IGNORED_INVALID("消息不合法(缺 msgid / JSON 解析失败),忽略");

        private final String desc;

        ConsumeResult(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public ConsumeResult onMessage(String raw) {
        JsonNode node;
        try {
            node = MAPPER.readTree(raw);
        } catch (Exception e) {
            log.warn("MQ 消息 JSON 解析失败,丢弃: {}", raw);
            return ConsumeResult.IGNORED_INVALID;
        }

        String msgId = node.path("msgid").asText("");
        String msgType = node.path("msgtype").asText("");
        String roomId = node.path("roomid").asText("");

        if (!"text".equals(msgType)) {
            log.debug("忽略非 text 消息: msgtype={}", msgType);
            return ConsumeResult.IGNORED_NOT_TEXT;
        }
        if (roomId.isBlank()) {
            log.debug("忽略 roomid 为空的消息(单聊非群消息)");
            return ConsumeResult.IGNORED_NO_ROOMID;
        }
        if (msgId.isBlank()) {
            log.warn("msgid 为空,无法保证幂等,丢弃: {}", raw);
            return ConsumeResult.IGNORED_INVALID;
        }
        Long configured = configMapper.selectCount(new LambdaQueryWrapper<CargoDeclareInfo>()
                .eq(CargoDeclareInfo::getRoomId, roomId)
                .eq(CargoDeclareInfo::getValid, 1));
        if (configured == null || configured == 0) {
            // 决策点 D2 默认策略:未配置群直接丢弃 + 日志计数
            log.debug("群未配置,忽略: roomId={}", roomId);
            return ConsumeResult.IGNORED_ROOM_NOT_CONFIGURED;
        }
        Long existing = recordMapper.selectCount(new LambdaQueryWrapper<EnterpriseWechatRecord>()
                .eq(EnterpriseWechatRecord::getMsgId, msgId));
        if (existing != null && existing > 0) {
            // 前置过滤(减少无效 insert);真正兜底仍是唯一键冲突
            log.debug("msgid 已存在,幂等忽略: {}", msgId);
            return ConsumeResult.DUPLICATED;
        }

        EnterpriseWechatRecord record = new EnterpriseWechatRecord();
        record.setMsgId(msgId);
        record.setAction(node.path("action").asText("send"));
        record.setFromUser(node.path("from").asText("unknown"));
        record.setRoomId(roomId);
        record.setMsgType(msgType);
        record.setMsgContent(node.path("text").path("content").asText(""));
        long ts = node.path("msgtime").asLong(System.currentTimeMillis());
        record.setMsgTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));

        try {
            recordMapper.insert(record);
            log.info("消息入库: msgId={} room={} from={} content={}",
                    msgId, roomId, record.getFromUser(), record.getMsgContent());
            return ConsumeResult.CONSUMED;
        } catch (DataIntegrityViolationException e) {
            // uk_msg_id 冲突:MQ 至少一次投递下的重复消费,幂等忽略
            log.debug("msgid 唯一键冲突,幂等忽略: {}", msgId);
            return ConsumeResult.DUPLICATED;
        }
    }
}
