package org.xiaoxu.common.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.pojo.dto.SystemUserDTO;

/**
 * @className: UserConvert
 * @author: xiaoxu
 * @date: 2025/11/17 15:40
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface UserConvert {

    UserConvert INSTANCE =  Mappers.getMapper(UserConvert.class);

    @Mapping(source = "id", target = "id")
    SystemUserDTO mapToUser(SystemUsers users);
}
