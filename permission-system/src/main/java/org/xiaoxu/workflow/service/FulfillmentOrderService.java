package org.xiaoxu.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.workflow.dto.FulfillmentOrderCreateDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderQueryDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderUpdateDTO;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.vo.FulfillmentOrderVO;

/**
 * 履约单服务接口
 */
public interface FulfillmentOrderService extends IService<FulfillmentOrder> {

    FulfillmentOrderVO create(FulfillmentOrderCreateDTO dto, String applicant);

    FulfillmentOrderVO update(FulfillmentOrderUpdateDTO dto);

    IPage<FulfillmentOrderVO> pageQuery(FulfillmentOrderQueryDTO query);

    FulfillmentOrderVO getDetailById(Long id);

    void deleteById(Long id);

    FulfillmentOrderVO changeStatus(Long id, String status, String remark);

    FulfillmentOrderVO submitApproval(Long id, String applicant);
}
