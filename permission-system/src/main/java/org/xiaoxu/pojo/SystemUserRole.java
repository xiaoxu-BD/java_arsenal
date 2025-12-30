package org.xiaoxu.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class SystemUserRole extends BaseEntity {

  private Long id;
  private Long userId;
  private Long roleId;




}
