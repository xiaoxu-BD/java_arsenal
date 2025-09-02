package org.xiaoxu.web_boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.entity.ItUser;
import org.xiaoxu.web_boot.entity.vo.UserInfo;

public interface ItUserService extends IService<ItUser> {

    UserInfo getUserInfo();
}
