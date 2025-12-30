package org.xiaoxu.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class SystemRoleMenu extends BaseEntity{

  private Long id;
  private Long roleId;
  private Long menuId;

}
