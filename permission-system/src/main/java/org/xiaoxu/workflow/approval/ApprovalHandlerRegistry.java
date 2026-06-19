package org.xiaoxu.workflow.approval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批回调注册中心。启动时收集所有 {@link ApprovalHandler} Bean，
 * 以 {@link ApprovalHandler#supportProcessDefinitionKey()} 为键建立路由表。
 */
@Slf4j
@Component
public class ApprovalHandlerRegistry {

    private final Map<String, ApprovalHandler> handlers;

    public ApprovalHandlerRegistry(List<ApprovalHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        ApprovalHandler::supportProcessDefinitionKey,
                        h -> h,
                        (a, b) -> {
                            throw new IllegalStateException("存在重复的 processDefinitionKey 注册: "
                                    + a.getClass().getName() + " 与 " + b.getClass().getName());
                        }));
        log.info("审批回调注册完成，已注册业务: {}", handlers.keySet());
    }

    /**
     * 按 processDefinitionKey 获取对应的回调处理器。
     *
     * @param processDefinitionKey 流程定义 key
     * @return 对应的 Handler；未注册时返回 null
     */
    public ApprovalHandler get(String processDefinitionKey) {
        return handlers.get(processDefinitionKey);
    }

    /**
     * 由 processDefinitionKey + businessKey 解析业务单主键 id，
     * 供前端待办列表跳转详情页。未注册 handler 或解析失败时返回 null。
     */
    public Long getBusinessId(String processDefinitionKey, String businessKey) {
        ApprovalHandler handler = handlers.get(processDefinitionKey);
        if (handler == null) {
            return null;
        }
        return handler.getBusinessId(businessKey);
    }
}
