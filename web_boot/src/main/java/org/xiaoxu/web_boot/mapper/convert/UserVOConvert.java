package org.xiaoxu.web_boot.mapper.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.xiaoxu.web_boot.entity.User;
import org.xiaoxu.web_boot.entity.vo.UserVO;

import java.util.List;

@Mapper
public interface UserVOConvert {

   UserVOConvert INSTANCE =  Mappers.getMapper(UserVOConvert.class);
   @Mapping(target = "userId",source = "id")
   @Mapping(target = "userName",source = "name")
   UserVO  toUserVO(User user);



   List<UserVO> toUserVOList(List<User> userList);
}
