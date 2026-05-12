package org.xiaoxu.channel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.channel.domain.ChannelOperationLog;
import org.xiaoxu.channel.exception.ChannelException;
import org.xiaoxu.channel.mapper.ChannelLogMapper;
import org.xiaoxu.channel.sign.ChannelSignVerifier;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class ChannelCallbackController {

    private final ChannelLogMapper logMapper;
    private final ChannelSignVerifier signVerifier;

    @PostMapping("/{channelCode}")
    public Map<String, String> callback(@PathVariable String channelCode,
                                        @RequestHeader Map<String, String> headers,
                                        @RequestBody String body) {
        log.info("【回调】收到渠道回调: channelCode={}", channelCode);

        // ① 验签（防篡改）
        if (!signVerifier.verify(channelCode, headers, body)) {
            throw new ChannelException("回调签名验证失败: " + channelCode);
        }

        // ② 解析bizId（按渠道协议从body中提取，此处为骨架）
        String bizId = extractBizId(body);
        String callbackSeqNo = headers.getOrDefault("X-Callback-Seq", null);

        // ③ 幂等校验：通过 biz_id 查找流水
        ChannelOperationLog log = logMapper.selectByBiz(channelCode, bizId);
        if (log == null) {
//            log.warn("【回调】流水不存在: channelCode={}, bizId={}", channelCode, bizId);
            return Map.of("code", "FAIL", "msg", "流水不存在");
        }

        // ④ 防重放：已终态直接返回成功
        if ("SUCCEED".equals(log.getStatus()) || "FAILED".equals(log.getStatus())) {
//            log.info("【回调】已终态，幂等忽略: channelCode={}, bizId={}, status={}", channelCode, bizId, log.getStatus());
            return Map.of("code", "SUCCESS", "msg", "重复回调已忽略");
        }

        // ⑤ 序列号防重放
        if (callbackSeqNo != null) {
            ChannelOperationLog seqLog = logMapper.selectByCallbackSeqNo(channelCode, callbackSeqNo);
            if (seqLog != null) {
//                log.info("【回调】回调序列号重复: channelCode={}, seqNo={}", channelCode, callbackSeqNo);
                return Map.of("code", "SUCCESS", "msg", "重复序列号已忽略");
            }
        }

        // ⑥ 更新流水 + 触发业务
        log.setCallbackBody(body);
        log.setCallbackSeqNo(callbackSeqNo);
        log.setStatus("SUCCEED");
        log.setFinishedAt(LocalDateTime.now());
        logMapper.updateStatus(log);

        // TODO: 触发业务后续逻辑（发事件/调业务服务）
        // businessEventPublisher.publish(new ChannelCallbackEvent(bizId));

//        log.info("【回调】处理完成: channelCode={}, bizId={}", channelCode, bizId);
        return Map.of("code", "SUCCESS");
    }

    /**
     * 从回调报文中提取bizId — 按渠道协议实现
     * 此处为骨架，实际用JSON解析
     */
    private String extractBizId(String body) {
        // TODO: 按渠道协议解析 body，提取 biz_id
        // 示例: return JsonPath.read(body, "$.biz_id");
        throw new ChannelException("需实现 extractBizId 方法");
    }
}
