package org.xiaoxu.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class SystemMenu extends BaseEntity {

  private Long id;
  private String name;
  private String permission;
  private Long type;
  private Long sort;
  private Long parentId;
  private String path;
  private String icon;
  private String component;
  private String componentName;
  private Long status;
  private String visible;
  private String keepAlive;
  private String alwaysShow;

}
