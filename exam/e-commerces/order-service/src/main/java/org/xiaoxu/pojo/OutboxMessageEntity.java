package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@TableName(value = "outbox_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessageEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String aggregateType;
  private String aggregateId;
  private String eventType;

  @JsonFormat
  private String payload;
  private LocalDateTime createdAt;
  private LocalDateTime processedAt;
}
