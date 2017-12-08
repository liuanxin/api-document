package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 想要在某个类或接口上忽略, 使用此注释 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiIgnore {
    boolean value() default true;
}
