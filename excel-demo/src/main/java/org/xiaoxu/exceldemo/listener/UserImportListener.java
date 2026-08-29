package org.xiaoxu.exceldemo.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.exception.ExcelDataConvertException;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.xiaoxu.exceldemo.dto.ExcelTask;
import org.xiaoxu.exceldemo.dto.ImportResult;
import org.xiaoxu.exceldemo.dto.UserImportErrorRow;
import org.xiaoxu.exceldemo.dto.UserImportRow;
import org.xiaoxu.exceldemo.entity.DemoUser;
import org.xiaoxu.exceldemo.mapper.DemoUserMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 导入核心逻辑: 逐行回调 -> 三层校验 -> 每 1000 行批量入库。
 * 三层校验:
 *   格式层  类型转换失败(日期/数字), 由 onException 捕获, 行数据已丢只能报行号
 *   字段层  必填/长度/正则/枚举/范围, checkRow 里用最直白的 if
 *   业务层  文件内手机号去重 + 库内已存在, 库内查重按批 IN, 不逐行查库
 */
public class UserImportListener implements ReadListener<UserImportRow> {

    private static final int BATCH_SIZE = 1000;
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final BigDecimal MAX_SALARY = new BigDecimal("1000000");

    private final DemoUserMapper userMapper;
    private final ExcelTask task;

    private long rowCount;
    private long successCount;
    private long failCount;

    /** 文件内已出现的手机号, 防同文件重复 */
    private final Set<String> seenPhones = new HashSet<>();
    private final List<UserImportErrorRow> errors = new ArrayList<>();

    /** 待入库的行, 连同它对应的 Excel 行号(报错要指到行) */
    private record BufferedRow(UserImportRow row, int excelRowNo) {
    }

    private final List<BufferedRow> buffer = new ArrayList<>(BATCH_SIZE);

    public UserImportListener(DemoUserMapper userMapper, ExcelTask task) {
        this.userMapper = userMapper;
        this.task = task;
    }

    @Override
    public void invoke(UserImportRow row, AnalysisContext context) {
        rowCount++;
        task.setProcessed(rowCount);

        int excelRowNo = context.readRowHolder().getRowIndex() + 1;
        String fieldError = checkRow(row);
        if (fieldError != null) {
            failCount++;
            task.setFailCount(failCount);
            errors.add(new UserImportErrorRow(row, excelRowNo, fieldError));
            return;
        }
        if (!seenPhones.add(row.getPhone())) {
            failCount++;
            task.setFailCount(failCount);
            errors.add(new UserImportErrorRow(row, excelRowNo, "手机号在文件内重复"));
            return;
        }
        buffer.add(new BufferedRow(row, excelRowNo));
        if (buffer.size() == BATCH_SIZE) {
            flushBuffer();
        }
    }

    /** 格式层: 转换失败的行不会进 invoke, 在这里记下错误继续读后面的行 */
    @Override
    public void onException(Exception e, AnalysisContext context) {
        if (e instanceof ExcelDataConvertException convert) {
            rowCount++;
            task.setProcessed(rowCount);
            failCount++;
            task.setFailCount(failCount);
            int excelRowNo = convert.getRowIndex() + 1;
            int excelColNo = convert.getColumnIndex() + 1;
            errors.add(new UserImportErrorRow(excelRowNo,
                    "第 " + excelColNo + " 列数据格式错误: " + convert.getMessage()));
            return;
        }
        // 文件损坏等异常直接终止导入
        throw new RuntimeException(e);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flushBuffer();
    }

    /** 字段层校验, 返回 null 表示通过 */
    private String checkRow(UserImportRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            return "姓名不能为空";
        }
        if (row.getName().length() > 20) {
            return "姓名超过 20 个字";
        }
        if (row.getPhone() == null || !PHONE.matcher(row.getPhone()).matches()) {
            return "手机号格式不合法";
        }
        if (row.getEmail() != null && !row.getEmail().isBlank()
                && !EMAIL.matcher(row.getEmail()).matches()) {
            return "邮箱格式不合法";
        }
        if (!UserImportRow.DEPARTMENTS.contains(row.getDeptName())) {
            return "部门必须是: " + String.join("/", UserImportRow.DEPARTMENTS);
        }
        if (row.getSalary() == null || row.getSalary().compareTo(BigDecimal.ZERO) <= 0
                || row.getSalary().compareTo(MAX_SALARY) > 0) {
            return "薪资必须在 (0, 100万] 之间";
        }
        if (row.getHireDate() == null) {
            return "入职日期不能为空";
        }
        return null;
    }

    /** 业务层: 一批手机号一次 IN 查库, 已存在的记错误, 其余批量入库 */
    private void flushBuffer() {
        if (buffer.isEmpty()) {
            return;
        }
        List<String> phones = buffer.stream().map(b -> b.row().getPhone()).toList();
        Set<String> existsInDb = new HashSet<>();
        for (DemoUser user : userMapper.selectList(
                new LambdaQueryWrapper<DemoUser>().in(DemoUser::getPhone, phones))) {
            existsInDb.add(user.getPhone());
        }

        List<DemoUser> toInsert = new ArrayList<>(buffer.size());
        for (BufferedRow buffered : buffer) {
            if (existsInDb.contains(buffered.row().getPhone())) {
                failCount++;
                task.setFailCount(failCount);
                errors.add(new UserImportErrorRow(buffered.row(), buffered.excelRowNo(), "手机号已存在于数据库"));
            } else {
                toInsert.add(toUser(buffered.row()));
            }
        }
        if (!toInsert.isEmpty()) {
            Db.saveBatch(toInsert);
            successCount += toInsert.size();
            task.setSuccessCount(successCount);
        }
        buffer.clear();
    }

    private DemoUser toUser(UserImportRow row) {
        DemoUser user = new DemoUser();
        user.setName(row.getName());
        user.setPhone(row.getPhone());
        user.setEmail(row.getEmail());
        user.setDeptName(row.getDeptName());
        user.setSalary(row.getSalary());
        user.setHireDate(row.getHireDate());
        return user;
    }

    public ImportResult getResult() {
        return new ImportResult(rowCount, successCount, failCount, errors);
    }
}
