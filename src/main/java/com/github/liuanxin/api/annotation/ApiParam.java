package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    String value();

    /** param name, if set will ignore paramName */
    String name() default "";

    /** when customize type comment, use this(example for Enum) */
    String dataType() default "";

    ParamType paramType() default ParamType.Query;

    String example() default "";

    boolean must() default false;

    /** in html, true will be textarea, default was input. */
    boolean textarea() default false;
}
