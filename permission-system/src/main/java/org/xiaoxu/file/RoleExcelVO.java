package org.xiaoxu.file;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色列表导出 Excel 模型
 */
@Data
public class RoleExcelVO implements Serializable {

    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("角色名称")
    @ColumnWidth(20)
    private String name;

    @ExcelProperty("角色编码")
    @ColumnWidth(20)
    private String code;

    @ExcelProperty("排序")
    @ColumnWidth(10)
    private Long sort;

    @ExcelProperty("数据权限")
    @ColumnWidth(18)
    private String dataScope;

    @ExcelProperty("创建时间")
    @ColumnWidth(22)
    private LocalDateTime createTime;
}
