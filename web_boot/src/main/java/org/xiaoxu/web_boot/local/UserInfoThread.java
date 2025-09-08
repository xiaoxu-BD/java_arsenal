package org.xiaoxu.web_boot.local;

import org.xiaoxu.web_boot.entity.vo.UserVO;

/**
 * @className: UserInfoThread
 * @author: xiaoxu
 * @date: 2025/9/4 13:52
 * @Version: 1.0
 * @description:
 */
public class UserInfoThread {

    private static final  ThreadLocal<String>  USER_VO_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserInfoThread(String idCard) {
        USER_VO_THREAD_LOCAL.set(idCard);
    }


    public static String getUserThread() {
         return    USER_VO_THREAD_LOCAL.get();
    }

    public static void removeUserInfoThread() {
        USER_VO_THREAD_LOCAL.remove();
    }



}
