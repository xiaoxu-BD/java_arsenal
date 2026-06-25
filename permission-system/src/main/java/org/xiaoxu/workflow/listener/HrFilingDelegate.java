package org.xiaoxu.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * HR备案节点 - 自动处理
 * 用于请假天数 <= 3 天的情况，自动完成HR备案
 */
@Slf4j
public class HrFilingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String businessKey = execution.getProcessInstanceBusinessKey();
        
        log.info("HR备案处理 - 流程实例: {}, 业务Key: {}", processInstanceId, businessKey);
        
        // 这里可以添加HR备案的业务逻辑
        // 例如：
        // 1. 记录HR备案日志
        // 2. 发送通知给HR
        // 3. 更新请假单状态
        
        log.info("HR备案完成 - 流程实例: {}", processInstanceId);
    }
}
