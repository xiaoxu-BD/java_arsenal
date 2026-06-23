package org.xiaoxu.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.workflow.dto.DeployProcessRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程定义管理 — 供可视化设计器使用
 */
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessDefinitionController {

    private final RepositoryService repositoryService;

    /**
     * 查询所有已部署的流程定义
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().desc()
                .list();

        List<Map<String, Object>> result = definitions.stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("key", d.getKey());
            map.put("version", d.getVersion());
            map.put("deploymentId", d.getDeploymentId());
            map.put("suspended", d.isSuspended());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 部署 BPMN XML
     */
    @PostMapping("/deploy")
    public Result<?> deploy(@RequestBody @Valid DeployProcessRequest request) {
        String xml = request.getXml();
        String name = request.getName();

        if (name == null || name.isBlank()) {
            name = "未命名流程";
        }

        // 从 XML 中提取 process id 作为资源名
        String resourceName = name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_") + ".bpmn20.xml";

        Deployment deployment = repositoryService.createDeployment()
                .name(name)
                .addString(resourceName, xml)
                .deploy();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deploymentId", deployment.getId());
        data.put("name", deployment.getName());
        data.put("deployTime", deployment.getDeploymentTime());
        return Result.success(data);
    }

    /**
     * 获取已部署流程的 BPMN XML（用于编辑器回显）
     */
    @GetMapping("/xml/{processDefinitionId}")
    public Result<Map<String, Object>> getXml(@PathVariable String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            return Result.error(404, "流程定义不存在");
        }

        InputStream stream = repositoryService.getResourceAsStream(
                definition.getDeploymentId(), definition.getResourceName());
        String xml;
        try (stream) {
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return Result.error(500, "读取 BPMN XML 失败");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", definition.getId());
        data.put("name", definition.getName());
        data.put("key", definition.getKey());
        data.put("version", definition.getVersion());
        data.put("xml", xml);
        return Result.success(data);
    }

    /**
     * 挂起流程定义
     */
    @PutMapping("/suspend/{processDefinitionId}")
    public Result<?> suspend(@PathVariable String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
        return Result.success("已挂起");
    }

    /**
     * 激活流程定义
     */
    @PutMapping("/activate/{processDefinitionId}")
    public Result<?> activate(@PathVariable String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
        return Result.success("已激活");
    }

    /**
     * 删除部署（连同历史一起删，慎用）
     */
    @DeleteMapping("/delete/{deploymentId}")
    public Result<?> delete(@PathVariable String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
        return Result.success("已删除");
    }
}
