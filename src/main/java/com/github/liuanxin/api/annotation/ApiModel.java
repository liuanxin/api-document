package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiModel {

    /** @return desc */
    String value() default "";

    /** @return name, if set will ignore param name or file name */
    String name() default "";

    /** @return if type was custom. can be: int、long、float、double、date、phone、email、url、ipv4 */
    String dataType() default "";

    /** @return return example, only if the field is a string or underlying data type(including BigInteger and BigDecimal) */
    String example() default "";


    // The above attributes are used on the request parameters and return fields, and the following attributes are used on the request parameters.


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
