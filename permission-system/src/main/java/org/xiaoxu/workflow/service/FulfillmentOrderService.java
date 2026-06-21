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

    /**
     * 提交审批（支持指定流程 key）
     * @param id 履约单ID
     * @param applicant 申请人
     * @param processKey 流程定义 key，为 null 时使用默认值 "fulfillment-approval"
     */
    FulfillmentOrderVO submitApproval(Long id, String applicant, String processKey);
}
