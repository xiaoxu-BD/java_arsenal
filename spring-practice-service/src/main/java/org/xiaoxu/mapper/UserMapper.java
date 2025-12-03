package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.Users;

/**
 * @className: UserMapper
 * @author: xiaoxu
 * @date: 2025/12/3 20:09
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface UserMapper extends BaseMapper<Users> {
}
