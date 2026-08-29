package org.xiaoxu.exceldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.exceldemo.entity.DemoUser;

@Mapper
public interface DemoUserMapper extends BaseMapper<DemoUser> {
}
