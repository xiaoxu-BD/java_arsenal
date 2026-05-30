package org.xiaoxu.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemMenu extends BaseEntity {

  @TableId(type = IdType.AUTO)
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
