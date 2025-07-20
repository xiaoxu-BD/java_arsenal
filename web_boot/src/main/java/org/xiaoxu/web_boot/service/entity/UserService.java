package org.xiaoxu.web_boot.service.entity;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.common.request.RegisterParam;
import org.xiaoxu.web_boot.entity.User;
import org.xiaoxu.web_boot.entity.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {
    void addUser( RegisterParam register);

    List<UserVO> getUser();

    void updateUser(UserVO userVO);

    UserVO getByIdCard(String idCard);
}
