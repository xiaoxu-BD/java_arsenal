package org.xiaoxu.file;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 全部任务列表导出 Excel 模型
 */
@Data
public class WorkflowItemExcelVO implements Serializable {

    @ExcelProperty("业务类型")
    @ColumnWidth(12)
    private String businessType;

    @ExcelProperty("业务编号")
    @ColumnWidth(28)
    private String businessKey;

    @ExcelProperty("标题")
    @ColumnWidth(30)
    private String title;

    @ExcelProperty("申请人")
    @ColumnWidth(15)
    private String applicant;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String status;

    @ExcelProperty("金额")
    @ColumnWidth(15)
    private BigDecimal amount;

    @ExcelProperty("请假天数")
    @ColumnWidth(12)
    private BigDecimal leaveDay;

    @ExcelProperty("创建时间")
    @ColumnWidth(22)
    private Date createTime;
}
