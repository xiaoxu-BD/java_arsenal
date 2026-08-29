package org.xiaoxu.exceldemo.dto;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;
import org.apache.fesod.sheet.annotation.format.NumberFormat;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import org.apache.fesod.sheet.annotation.write.style.HeadRowHeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 导入行模型。两级表头: 第一行是"基本信息/工作信息", 第二行是列名, 读取时 headRowNumber(2)。
 * 同一个模型同时用于生成导入模板, 所以列上带了写侧注解(列宽/格式)。
 */
@Data
@HeadRowHeight(20)
@ColumnWidth(15)
public class UserImportRow {

    /** 部门列的合法取值: 模板下拉、导入校验、造数三处共用这一份 */
    public static final List<String> DEPARTMENTS = List.of("研发部", "市场部", "财务部", "人事部");

    @ExcelProperty({"基本信息", "姓名"})
    private String name;

    @ExcelProperty({"基本信息", "手机号"})
    private String phone;

    /** 可空字段, 有值才校验格式 */
    @ExcelProperty({"基本信息", "邮箱"})
    @ColumnWidth(25)
    private String email;

    @ExcelProperty({"基本信息", "部门"})
    private String deptName;

    @ExcelProperty({"工作信息", "薪资"})
    private BigDecimal salary;

    @ExcelProperty({"工作信息", "入职日期"})
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate hireDate;
}
