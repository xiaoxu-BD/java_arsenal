package org.xiaoxu.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemRole extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String name;
  private String code;
  private Long sort;
  private Long dataScope;
  private String dataScopeDeptIds;


}
