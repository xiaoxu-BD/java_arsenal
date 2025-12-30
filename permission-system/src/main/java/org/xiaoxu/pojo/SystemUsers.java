package org.xiaoxu.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class SystemUsers  extends BaseEntity{

  private Long id;
  private String username;
  private String password;
  private String nickname;
  private String remark;
  private Long deptId;
  private String postIds;
  private String email;
  private String mobile;
  private Long sex;
  private String avatar;

}
