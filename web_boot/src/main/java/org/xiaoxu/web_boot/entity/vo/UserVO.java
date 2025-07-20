package org.xiaoxu.web_boot.entity.vo;

import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;

/**
 * @className: UserVO
 * @author: xiaoxu
 * @date: 2025/9/3 9:44
 * @Version: 1.0
 * @description:
 */

public class UserVO {
    private Long userId;
    private String userName;
    private String sex;
    private String idCard;

    public String getIdCard() {
        return idCard;

    }
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
