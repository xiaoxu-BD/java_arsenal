package org.xiaoxu.web_boot.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.ItUser;
import org.xiaoxu.web_boot.entity.vo.UserInfo;
import org.xiaoxu.web_boot.mapper.ItUserMapper;
import org.xiaoxu.web_boot.mapper.convert.UserConvertor;
import org.xiaoxu.web_boot.service.ItUserService;

import java.util.List;

/**
 * @className: ItUserServiceImpl
 * @author: xiaoxu
 * @date: 2025/9/2 15:07
 * @Version: 1.0
 * @description:
 */

@Service
public class ItUserServiceImpl extends ServiceImpl<ItUserMapper, ItUser> implements ItUserService {


    @Override
    public UserInfo getUserInfo() {
        List<ItUser> userList = this.list();
        ItUser user = new ItUser();
        if (userList != null && userList.size() > 0) {
            user =   userList.get(0);
        }
        return UserConvertor.INSTANCE.mapToVo(user);
    }
}
