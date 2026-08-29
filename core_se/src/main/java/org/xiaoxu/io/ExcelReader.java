package org.xiaoxu.io;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    
    /**
     * 读取Excel文件（支持 .xls 和 .xlsx）
     * @param filePath Excel文件路径
     * @return 所有数据，List<Map<String, Object>> 格式
     */
    public static List<Map<String, Object>> readExcel(String filePath) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (
            // 1. 创建BufferedInputStream包装FileInputStream（提升性能）
            BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(filePath)
            )
        ) {
            // 2. 根据文件后缀创建不同的Workbook
            Workbook workbook;
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(bis); // Excel 2007+
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(bis); // Excel 97-2003
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + filePath);
            }
            
            // 3. 获取第一个Sheet
            Sheet sheet = workbook.getSheetAt(0);
            
            // 4. 获取表头（第一行）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return result;
            }
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
            
            // 5. 读取数据行（从第二行开始）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = getCellValueAsString(cell);
                    rowData.put(headers.get(j), value);
                }
                result.add(rowData);
            }
            
            workbook.close();
            System.out.println("✅ 成功读取Excel，共 " + result.size() + " 行数据");
            
        } catch (Exception e) {
            System.err.println("❌ 读取Excel失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 通用方法：获取Cell的值（支持所有数据类型）
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字（避免科学计数法）
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
    
    // ========== 测试方法 ==========
    public static void main(String[] args) {
        // 读取Excel文件
        String filePath = "C:\\Users\\tingq\\Downloads\\用户导出_2026-08-29 (1).xlsx";
        List<Map<String, Object>> data = readExcel(filePath);
        
        // 打印结果
        if (!data.isEmpty()) {
            System.out.println("\n=== 数据预览（前3行）===");
            for (int i = 0; i < Math.min(3, data.size()); i++) {
                System.out.println("第" + (i + 2) + "行: " + data.get(i));
            }
        }
    }
}