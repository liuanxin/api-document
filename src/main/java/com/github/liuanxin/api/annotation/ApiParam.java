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

    /** @return if type was custom. can be: int、long、float、double、password、date、phone、email、url、ipv4 */
    String dataType() default "";

    /** @return param example */
    String example() default "";

    /** @return Header or Query */
    ParamType paramType() default ParamType.Query;

    /** @return if param has @RequestParam(required = true) etc..., this set will ignore */
    boolean required() default false;

    /** @return in html, true will be textarea, default was input. */
    boolean textarea() default false;

    /** @return pattern of the date parameter */
    String datePattern() default "";

    /** @return show html style in page */
    String style() default "";
}
