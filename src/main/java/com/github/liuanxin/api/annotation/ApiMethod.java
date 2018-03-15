package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiMethod {

    String title();

    String desc() default "";

    String develop();

    /** more forward when smaller */
    int index() default Integer.MAX_VALUE;
}
