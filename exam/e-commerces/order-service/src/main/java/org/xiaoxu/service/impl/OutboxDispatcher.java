package org.xiaoxu.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.mapper.OutboxMapper;
import org.xiaoxu.pojo.OutboxMessageEntity;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxDispatcher {
  private final OutboxMapper outboxRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final int batchSize = 50;

  @Scheduled(fixedDelayString = "${app.outbox.dispatch-interval-ms:1000}")
  @Transactional
  public void dispatch() {
    List<OutboxMessageEntity> messages = outboxRepository.selectUnprocessedForUpdateSkipLocked(batchSize);
    for (OutboxMessageEntity m : messages) {
      try {
        // send to exchange, routing key = eventType
        rabbitTemplate.convertAndSend("order_events", m.getEventType(), m.getPayload());
        m.setProcessedAt(LocalDateTime.now());
        outboxRepository.insert(m);
      } catch (AmqpException ex) {
        // 发送失败，记录日志，下一次继续重试
        // 不抛异常以保证继续处理其它消息
      }
    }
  }
}
