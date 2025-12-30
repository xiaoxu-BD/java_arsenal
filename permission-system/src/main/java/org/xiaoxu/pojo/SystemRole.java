package org.xiaoxu.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class SystemRole extends BaseEntity{

  private Long id;
  private String name;
  private String code;
  private Long sort;
  private Long dataScope;
  private String dataScopeDeptIds;


}
