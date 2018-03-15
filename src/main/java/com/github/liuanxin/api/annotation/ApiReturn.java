package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturn {

    /** when customize type comment, use this(example for Enum) */
    String type() default "";

    /** return comment */
    String desc();
}
