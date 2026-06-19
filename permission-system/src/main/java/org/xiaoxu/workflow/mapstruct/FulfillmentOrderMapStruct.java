package org.xiaoxu.workflow.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.xiaoxu.workflow.dto.FulfillmentOrderCreateDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderUpdateDTO;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.vo.FulfillmentOrderVO;

/**
 * 履约单对象映射器
 */
@Mapper(componentModel = "spring")
public interface FulfillmentOrderMapStruct {

    FulfillmentOrderVO toVO(FulfillmentOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processInstId", ignore = true)
    @Mapping(target = "applicant", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "modifyTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    FulfillmentOrder toEntity(FulfillmentOrderCreateDTO dto);

    @Mapping(target = "orderNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processInstId", ignore = true)
    @Mapping(target = "applicant", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "modifyTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    FulfillmentOrder toEntity(FulfillmentOrderUpdateDTO dto);
}
