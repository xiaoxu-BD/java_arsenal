package org.xiaoxu.exceldemo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.exceldemo.dto.ExcelTask;
import org.xiaoxu.exceldemo.service.ExcelTaskService;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** 异步接口: 提交返回 taskId, 轮询进度, 结束后下载回执或导出产物 */
@RestController
@RequestMapping("/api/excel/async")
public class AsyncExcelController {

    private final ExcelTaskService taskService;

    public AsyncExcelController(ExcelTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/import")
    public ExcelTask startImport(@RequestParam("file") MultipartFile file) throws IOException {
        return taskService.startImport(file);
    }

    @PostMapping("/export")
    public ExcelTask startExport() {
        return taskService.startExport();
    }

    @GetMapping("/{taskId}")
    public ExcelTask progress(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    /** 导入任务下有错时是错误回执, 导出任务下是导出文件 */
    @GetMapping("/{taskId}/file")
    public void downloadFile(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        ExcelTask task = taskService.getTask(taskId);
        if (task.getFilePath() == null) {
            throw new IllegalStateException("任务没有可下载文件(可能尚未结束或全部成功): " + taskId);
        }
        File file = new File(task.getFilePath());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
