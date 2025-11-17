package org.xiaoxu.common.excepiton.user;

import org.xiaoxu.common.excepiton.BizException;
import org.xiaoxu.common.excepiton.ErrorCode;

/**
 * @className: UserException
 * @author: xiaoxu
 * @date: 2025/11/17 15:23
 * @Version: 1.0
 * @description:
 */
public class UserException  extends BizException {


    private ErrorCode errorCode;


    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
