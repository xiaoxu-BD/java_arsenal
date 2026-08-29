package org.xiaoxu.exceldemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PreDestroy;
import org.apache.fesod.sheet.FesodSheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.exceldemo.dto.ExcelTask;
import org.xiaoxu.exceldemo.dto.ImportResult;
import org.xiaoxu.exceldemo.dto.UserExportRow;
import org.xiaoxu.exceldemo.dto.UserImportErrorRow;
import org.xiaoxu.exceldemo.dto.UserImportRow;
import org.xiaoxu.exceldemo.entity.DemoUser;
import org.xiaoxu.exceldemo.listener.UserImportListener;
import org.xiaoxu.exceldemo.mapper.DemoUserMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步大表: 上传/触发后立即返回 taskId, 后台线程跑, 进度查 GET /async/{taskId}。
 * demo 的任务表在内存, 重启即失; 生产上要落库/Redis 并支持断点。
 */
@Service
public class ExcelTaskService {

    /** 导出每页捞多少行, 也是内存里同时驻留的最大行数 */
    private static final int EXPORT_PAGE_SIZE = 5000;
    /** 单 sheet 写到 50 万行就换下一个, 远离 xlsx 单 sheet 1048576 的硬上限 */
    private static final long ROWS_PER_SHEET = 500_000;

    private final DemoUserMapper userMapper;
    private final String storageDir;

    private final Map<String, ExcelTask> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ExcelTaskService(DemoUserMapper userMapper,
                            @Value("${excel.storage-dir}") String storageDir) throws IOException {
        this.userMapper = userMapper;
        this.storageDir = storageDir;
        Files.createDirectories(Path.of(storageDir));
    }

    public ExcelTask startImport(MultipartFile file) throws IOException {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        // 先把上传件落盘再异步读, 避免临时文件被容器清理
        File uploadFile = new File(storageDir, taskId + "-导入原件.xlsx");
        file.transferTo(uploadFile.getAbsoluteFile());

        ExcelTask task = newTask(taskId, ExcelTask.TYPE_IMPORT);
        tasks.put(taskId, task);
        executor.submit(() -> runImport(task, uploadFile));
        return task;
    }

    public ExcelTask startExport() {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        ExcelTask task = newTask(taskId, ExcelTask.TYPE_EXPORT);
        // 导出前先数总数, 进度才能算百分比; 导入读不到总数, 只有 processed
        task.setTotal(userMapper.selectCount(null));
        tasks.put(taskId, task);
        executor.submit(() -> runExport(task));
        return task;
    }

    public ExcelTask getTask(String taskId) {
        ExcelTask task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已随重启丢失: " + taskId);
        }
        return task;
    }

    private ExcelTask newTask(String taskId, String type) {
        ExcelTask task = new ExcelTask();
        task.setId(taskId);
        task.setType(type);
        task.setStatus(ExcelTask.STATUS_RUNNING);
        return task;
    }

    private void runImport(ExcelTask task, File uploadFile) {
        try {
            UserImportListener listener = new UserImportListener(userMapper, task);
            FesodSheet.read(uploadFile, UserImportRow.class, listener)
                    .headRowNumber(2)
                    .sheet()
                    .doRead();
            ImportResult result = listener.getResult();
            if (result.failCount() > 0) {
                // 错误回执: 原数据 + 错误原因, 用户改完可以直接再传这个文件
                File reportFile = new File(storageDir, task.getId() + "-错误回执.xlsx");
                FesodSheet.write(reportFile, UserImportErrorRow.class)
                        .sheet("错误明细")
                        .doWrite(result.errors());
                task.setFilePath(reportFile.getAbsolutePath());
            }
            task.setStatus(ExcelTask.STATUS_DONE);
        } catch (Exception e) {
            task.setStatus(ExcelTask.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
        }
    }

    private void runExport(ExcelTask task) {
        File outFile = new File(storageDir, task.getId() + "-用户导出.xlsx");
        try (var writer = FesodSheet.write(outFile, UserExportRow.class).build()) {
            long lastId = 0;
            int sheetNo = 0;
            long rowsInSheet = 0;
            var sheet = FesodSheet.writerSheet(sheetNo, "用户数据" + (sheetNo + 1)).build();
            while (true) {
                // 游标翻页代替 limit offset, 深翻页不会越翻越慢
                List<DemoUser> page = userMapper.selectList(new LambdaQueryWrapper<DemoUser>()
                        .gt(DemoUser::getId, lastId)
                        .orderByAsc(DemoUser::getId)
                        .last("LIMIT " + EXPORT_PAGE_SIZE));
                if (page.isEmpty()) {
                    break;
                }
                writer.write(page.stream().map(UserExportRow::from).toList(), sheet);
                lastId = page.get(page.size() - 1).getId();
                task.setProcessed(task.getProcessed() + page.size());
                rowsInSheet += page.size();
                if (rowsInSheet >= ROWS_PER_SHEET) {
                    sheetNo++;
                    rowsInSheet = 0;
                    sheet = FesodSheet.writerSheet(sheetNo, "用户数据" + (sheetNo + 1)).build();
                }
            }
            task.setFilePath(outFile.getAbsolutePath());
            task.setStatus(ExcelTask.STATUS_DONE);
        } catch (Exception e) {
            task.setStatus(ExcelTask.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
