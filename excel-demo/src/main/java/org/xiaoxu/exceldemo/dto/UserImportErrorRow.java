package org.xiaoxu.exceldemo.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 错误回执行的模型: 原始数据 + 错误原因。同步导入时以 JSON 返回给前端,
 * 异步导入时写成"错误回执.xlsx"让用户改完再传。
 * 格式层错误(类型转换失败)的行拿不到原始值, 只有行号和原因。
 */
@Data
@ColumnWidth(15)
public class UserImportErrorRow {

    @ExcelIgnore
    private Integer rowIndex;

    @ExcelProperty({"基本信息", "姓名"})
    private String name;

    @ExcelProperty({"基本信息", "手机号"})
    private String phone;

    @ExcelProperty({"基本信息", "邮箱"})
    @ColumnWidth(25)
    private String email;

    @ExcelProperty({"基本信息", "部门"})
    private String deptName;

    @ExcelProperty({"工作信息", "薪资"})
    private BigDecimal salary;

    @ExcelProperty({"工作信息", "入职日期"})
    private LocalDate hireDate;

    @ExcelProperty({"导入结果", "错误原因"})
    @ColumnWidth(45)
    private String reason;

    public UserImportErrorRow(int rowIndex, String reason) {
        this.rowIndex = rowIndex;
        this.reason = reason;
    }

    public UserImportErrorRow(UserImportRow row, int rowIndex, String reason) {
        this.rowIndex = rowIndex;
        this.name = row.getName();
        this.phone = row.getPhone();
        this.email = row.getEmail();
        this.deptName = row.getDeptName();
        this.salary = row.getSalary();
        this.hireDate = row.getHireDate();
        this.reason = reason;
    }
}
