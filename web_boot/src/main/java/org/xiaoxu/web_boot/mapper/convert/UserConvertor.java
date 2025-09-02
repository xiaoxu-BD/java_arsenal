package org.xiaoxu.web_boot.mapper.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.xiaoxu.web_boot.entity.ItUser;
import org.xiaoxu.web_boot.entity.vo.UserInfo;

/**
 * @className: UserConvertor
 * @author: xiaoxu
 * @date: 2025/9/2 15:11
 * @Version: 1.0
 * @description:
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserConvertor {
    UserConvertor INSTANCE = Mappers.getMapper(UserConvertor.class);
    @Mapping(target = "userId", source = "id")
   public UserInfo mapToVo(ItUser user);
}
