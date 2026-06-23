package org.xiaoxu.workflow.service;

import org.xiaoxu.workflow.vo.ProcessDiagramVO;
import org.xiaoxu.workflow.vo.TaskVO;

import java.util.List;
import java.util.Map;

/**
 * Flowable 工作流核心服务接口
 */
public interface FlowableService {

    String deployProcess(String bpmnResourcePath);

    String startProcess(String processDefinitionKey, String businessKey, String initiator, Map<String, Object> variables);

    List<TaskVO> queryMyTasks(String assignee);

    List<TaskVO> queryCandidateTasks(String candidateGroup);

    void claimTask(String taskId, String userId);

    /**
     * 若任务尚未被任何人认领（assignee 为空），则自动认领给指定用户；
     * 已认领则跳过。返回是否真的执行了认领动作。
     */
    boolean claimIfUnassigned(String taskId, String userId);

    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * 完成任务并触发业务回调（一个事务内）：
     * <ol>
     *   <li>若任务未认领则自动认领给 username</li>
     *   <li>把 comment 写入 ACT_HI_COMMENT（持久化审批意见）</li>
     *   <li>把 approved/comment 作为流程变量 complete 当前任务</li>
     *   <li>注册 afterCommit 回调：通过 / 驳回时分别触发对应 ApprovalHandler</li>
     * </ol>
     * 把事务边界封装在 service 层，调用方（controller）无需再标 {@code @Transactional}。
     *
     * @param taskId   任务 id
     * @param username 当前操作人（用于自动认领）
     * @param approved true=通过, false=驳回
     * @param comment  审批意见（可空）
     */
    void completeAndCallback(String taskId, String username, boolean approved, String comment);

    List<Map<String, Object>> getHistoricActivities(String processInstanceId);

    String getBusinessKeyByTaskId(String taskId);

    String getProcessInstanceIdByTaskId(String taskId);

    /**
     * 根据任务 ID 获取所属流程定义 key（如 fulfillment-approval、leave-request），
     * 用作审批回调的路由键。须在 completeTask 之前调用。
     */
    String getProcessDefinitionKeyByTaskId(String taskId);

    boolean isProcessFinished(String processInstanceId);

    ProcessDiagramVO getProcessDiagramInfo(String processInstanceId);

    /**
     * 删除流程实例（级联删除历史）
     * @param processInstanceId 流程实例ID
     * @param reason 删除原因
     */
    void deleteProcessInstance(String processInstanceId, String reason);
}
