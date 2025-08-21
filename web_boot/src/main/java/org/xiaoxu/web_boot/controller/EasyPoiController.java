package org.xiaoxu.web_boot.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @className: EasyPoiController
 * @author: xiaoxu
 * @date: 2025/8/16 13:22
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/easyPoi")
public class EasyPoiController {

    @GetMapping("/create")
    public void  createExcel(HttpServletResponse response) throws Exception {
        //Workbook 工作簿
        Workbook workbook = new XSSFWorkbook();
        //创建工作表:
        Sheet sheet = workbook.createSheet();
        //创建表头
        Row headerRow = sheet.createRow(0);

        String[] headers = {"ID", "姓名", "部门", "薪资", "入职日期"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建数据行
        Object[][] data = {
                {1, "张三", "技术部", 8000.0, new Date()},
                {2, "李四", "销售部", 6000.0, new Date()},
                {3, "王五", "人事部", 5500.0, new Date()}
        };
        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                Object value = data[i][j];

                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                }
            }
        }
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=employee.xlsx");

        // 写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();

    }
}


