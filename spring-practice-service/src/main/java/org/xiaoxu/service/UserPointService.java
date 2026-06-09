package org.xiaoxu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.domain.UserPoints;

public interface UserPointService extends IService<UserPoints> {

    /**
     * 用户签到
     * @param userId 用户ID
     * @return 是否签到成功
     */
    Boolean signIn(Long userId);

    /**
     * 查询今日是否已签到
     * @param userId 用户ID
     * @return 是否已签到
     */
    Boolean hasSignedToday(Long userId);
}
