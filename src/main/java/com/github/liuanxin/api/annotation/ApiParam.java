package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    /** @return  param desc */
    String value() default "";

    /** @return param name, if set will ignore paramName */
    String name() default "";

    /** @return if type was custom can use(for example: enum, but param type was be int). can be: int、long、float、double、phone、email、url、ipv4 */
    String dataType() default "";

    /** @return param example */
    String example() default "";

    /** @return Header or Query */
    ParamType paramType() default ParamType.Query;

    /** @return if param has @RequestParam(required = true) etc..., this set will ignore */
    boolean must() default false;

    /** @return in html, true will be textarea, default was input. */
    boolean textarea() default false;

    /** @return show html style in page */
    String style() default "";
}
