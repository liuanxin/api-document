package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiToken {

    /** @return param name */
    String name();

    /** @return param desc */
    String desc() default "";

    /** @return param example */
    String example() default "";

    /** @return if type was custom can use(for example: enum, but param type was be int). can be: int、long、float、double、date、phone、email、url、ipv4 */
    String dataType() default "";

    /** @return Header or Query */
    ParamType paramType() default ParamType.Header;

    /** @return if param has @RequestParam(required = true) etc..., this set will ignore */
    boolean must() default false;

    /** @return in html, true will be textarea, default was input. */
    boolean textarea() default false;

    /** @return show html style in page */
    String style() default "";
}
