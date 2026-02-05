package org.xiaoxu.web_boot.entity.mapstruct;

import org.mapstruct.Mapper;
import org.xiaoxu.web_boot.entity.ItUser;
import org.xiaoxu.web_boot.entity.vo.UserVO;

@Mapper
public interface ReviUserMapper {


    UserVO toVO(ItUser itUser);
}
