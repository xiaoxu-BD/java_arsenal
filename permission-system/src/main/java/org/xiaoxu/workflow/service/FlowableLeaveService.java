package org.xiaoxu.workflow.service;

import org.xiaoxu.workflow.vo.ApproveLeaveVO;

import java.util.Map;

/**
 * 请假流程服务接口
 */
public interface FlowableLeaveService {

    String startProcess(String processDefinitionKey, String businessKey, String days, Map<String, Object> variables);

    ApproveLeaveVO submitApproval(String userName, String identifier, String days);
}
