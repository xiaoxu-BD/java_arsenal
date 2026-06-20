package org.xiaoxu.common.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EasyExcel 导出工具类
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {}

    /**
     * 将数据写入 HttpServletResponse，输出为 Excel 文件流
     *
     * @param response  HTTP 响应
     * @param fileName  下载文件名（不含 .xlsx 后缀）
     * @param sheetName 工作表名称
     * @param clazz     Excel 数据模型类（带 @ExcelProperty 注解）
     * @param data      要写入的数据列表
     */
    public static <T> void write(HttpServletResponse response,
                                 String fileName,
                                 String sheetName,
                                 Class<T> clazz,
                                 List<T> data) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
    }
}
