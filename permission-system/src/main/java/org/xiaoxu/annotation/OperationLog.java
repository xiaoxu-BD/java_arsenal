package org.xiaoxu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务操作日志注解，标注在 Controller 方法上，AOP 自动记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作模块，如：用户管理、角色管理 */
    String module() default "";

    /** 操作类型，如：新增、修改、删除、导出 */
    String operation() default "";
}
