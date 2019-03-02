package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    String value();

    /** param name, if set will ignore paramName */
    String name() default "";

    /** if type was custom can use(for example: enum, but param type was be int). can be: int、long、float、double、phone、email、url、ipv4 */
    String dataType() default "";

    ParamType paramType() default ParamType.Query;

    String example() default "";

    boolean must() default false;

    /** in html, true will be textarea, default was input. */
    boolean textarea() default false;
}
