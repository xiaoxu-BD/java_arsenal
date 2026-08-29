package org.xiaoxu.exceldemo.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;
import org.apache.fesod.sheet.annotation.format.NumberFormat;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import org.apache.fesod.sheet.annotation.write.style.HeadRowHeight;
import org.xiaoxu.exceldemo.entity.DemoUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 导出行模型。比导入多 id/入库时间 两列, 共有列的两级表头与 UserImportRow 保持同名,
 * 这样导出的文件可以直接拿去再导入, 多出的列会被读取端按名匹配忽略。
 */
@Data
@HeadRowHeight(20)
@ColumnWidth(15)
public class UserExportRow {

    @ExcelProperty({"基本信息", "ID"})
    private Long id;

    @ExcelProperty({"基本信息", "姓名"})
    private String name;

    @ExcelProperty({"基本信息", "手机号"})
    private String phone;

    @ExcelProperty({"基本信息", "部门"})
    private String deptName;

    @ExcelProperty({"工作信息", "薪资"})
    @NumberFormat("#,##0.00")
    private BigDecimal salary;

    @ExcelProperty({"工作信息", "入职日期"})
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate hireDate;

    @ExcelProperty({"工作信息", "入库时间"})
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createTime;

    public static UserExportRow from(DemoUser user) {
        UserExportRow row = new UserExportRow();
        row.setId(user.getId());
        row.setName(user.getName());
        row.setPhone(user.getPhone());
        row.setDeptName(user.getDeptName());
        row.setSalary(user.getSalary());
        row.setHireDate(user.getHireDate());
        row.setCreateTime(user.getCreateTime());
        return row;
    }
}
