package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** want to ignore some url, use this */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiIgnore {

    boolean value() default true;
}
