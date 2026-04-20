package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.service.ExcelService;

import java.io.IOException;

/**
 * @className: ExcelUserEasyExcelController
 * @author: xiaoxu
 * @date: 2026/4/13 19:51
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api/file")
public class ExcelUserEasyExcelController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/import")
    public Result<?> importExcel(@RequestParam MultipartFile file) throws IOException {
        excelService.importCustomer(file);
        return Result.success();
    }
}
