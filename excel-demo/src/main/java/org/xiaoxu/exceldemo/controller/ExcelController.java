package org.xiaoxu.exceldemo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.exceldemo.dto.ImportResult;
import org.xiaoxu.exceldemo.service.ExcelService;

import java.io.IOException;

/** 同步接口: 小表直接跑完返回, 大文件会长时间占住请求线程, 别在生产这么干 */
@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        excelService.downloadTemplate(response);
    }

    @PostMapping("/import")
    public ImportResult syncImport(@RequestParam("file") MultipartFile file) throws IOException {
        return excelService.syncImport(file);
    }

    @GetMapping("/export")
    public void syncExport(HttpServletResponse response) throws IOException {
        excelService.syncExport(response);
    }
}
