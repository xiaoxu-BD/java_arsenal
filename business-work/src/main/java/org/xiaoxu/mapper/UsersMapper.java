package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.domain.Users;

@Mapper
public interface UsersMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Users record);

    int insertSelective(Users record);

    Users selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Users record);

    int updateByPrimaryKey(Users record);
}