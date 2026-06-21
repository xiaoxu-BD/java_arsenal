package org.xiaoxu.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemUsers extends BaseEntity {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String username;
  private String password;
  private String nickname;
  private String remark;
  private Long deptId;
  /** 直属上级用户ID */
  private Long managerId;
  private String postIds;
  private String email;
  private String mobile;
  private Long sex;
  private String avatar;
  /** 是否首次登录：1-是 0-否（邮箱注册用户默认 1，改密码后置 0） */
  private String firstLogin;

}
