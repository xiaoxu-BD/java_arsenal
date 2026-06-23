package org.xiaoxu.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部署流程定义请求
 */
@Data
public class DeployProcessRequest {

    @NotBlank(message = "BPMN XML 不能为空")
    private String xml;

    /**
     * 流程名称（可选，默认"未命名流程"）
     */
    private String name;
}
