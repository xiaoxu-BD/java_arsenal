package org.xiaoxu.exceldemo.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.fesod.sheet.FesodSheet;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.exceldemo.dto.ExcelTask;
import org.xiaoxu.exceldemo.dto.ImportResult;
import org.xiaoxu.exceldemo.dto.UserExportRow;
import org.xiaoxu.exceldemo.dto.UserImportRow;
import org.xiaoxu.exceldemo.handler.DeptDropdownSheetHandler;
import org.xiaoxu.exceldemo.listener.UserImportListener;
import org.xiaoxu.exceldemo.mapper.DemoUserMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/** 同步小表闭环: 模板下载 / 导入 / 导出。大文件请走 ExcelTaskService 的异步接口 */
@Service
public class ExcelService {

    private final DemoUserMapper userMapper;

    public ExcelService(DemoUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 空表头 + 部门下拉, 不放示例行, 避免用户忘删示例被导入 */
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        prepareXlsxResponse(response, "用户导入模板");
        FesodSheet.write(response.getOutputStream(), UserImportRow.class)
                .registerWriteHandler(new DeptDropdownSheetHandler())
                .sheet("用户导入模板")
                .doWrite(List.of());
    }

    public ImportResult syncImport(MultipartFile file) throws IOException {
        ExcelTask counter = new ExcelTask();
        UserImportListener listener = new UserImportListener(userMapper, counter);
        FesodSheet.read(file.getInputStream(), UserImportRow.class, listener)
                .headRowNumber(2)
                .sheet()
                .doRead();
        return listener.getResult();
    }

    /** 全量查一次内存里拼完再写, 只适合小表, 大表走异步游标翻页 */
    public void syncExport(HttpServletResponse response) throws IOException {
        prepareXlsxResponse(response, "用户导出_" + LocalDate.now());
        List<UserExportRow> rows = userMapper.selectList(null).stream()
                .map(UserExportRow::from)
                .toList();
        FesodSheet.write(response.getOutputStream(), UserExportRow.class)
                .sheet("用户数据")
                .doWrite(rows);
    }

    private void prepareXlsxResponse(HttpServletResponse response, String fileName) {
        //设置内容类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // filename*=RFC 5987 写法, 中文文件名各浏览器都稳
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded + ".xlsx");
    }
}
