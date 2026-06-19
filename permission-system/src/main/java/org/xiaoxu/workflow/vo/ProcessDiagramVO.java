package org.xiaoxu.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程图信息 VO
 */
@Data
public class ProcessDiagramVO implements Serializable {

    private String processInstanceId;
    private String processDefinitionId;
    private List<String> finishedNodes;
    private List<String> finishedFlows;
    private List<String> activeNodes;
    private String bpmnXml;
}
