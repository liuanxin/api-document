package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiToken {

    /** param name */
    String name();

    /** param desc */
    String desc() default "";

    /** param example */
    String example() default "";

    /** Header or Query */
    ParamType paramType() default ParamType.Header;

    /** if param has @RequestParam(required = true) etc..., this set will ignore */
    boolean must() default false;

    /** in html, true will be textarea, default was input. */
    boolean textarea() default false;
}
