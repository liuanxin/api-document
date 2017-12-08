package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口参数, 标注在方法参数或字段(当使用 dto 时)上 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {
    /** 参数名, 如果设置了将会无视方法名或字段名 */
    String name() default "";
    /** 参数类型, 当需要自定义时(比如枚举)有用 */
    String type() default "";
    /** 参数是否必须 */
    boolean must() default false;
    /** 参数示例 */
    String example();
    /** 参数说明 */
    String desc();
}
