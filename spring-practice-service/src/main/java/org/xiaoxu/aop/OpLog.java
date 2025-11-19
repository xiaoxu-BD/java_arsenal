package org.xiaoxu.aop;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    String scene() default "";
}
