package org.xiaoxu.exceldemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("demo_user")
public class DemoUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    private String email;

    private String deptName;

    private BigDecimal salary;

    private LocalDate hireDate;

    /** 插入时留 null 走数据库默认值, 查询时回填 */
    private LocalDateTime createTime;
}
