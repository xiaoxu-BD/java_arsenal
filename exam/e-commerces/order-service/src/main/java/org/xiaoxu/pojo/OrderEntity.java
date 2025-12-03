package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class OrderEntity implements Serializable {
  @TableId(type = IdType.AUTO)
  private String id;
  private String userId;
  private String status;
  private String payload;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
