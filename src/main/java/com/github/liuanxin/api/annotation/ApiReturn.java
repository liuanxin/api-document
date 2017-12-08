package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口返回结果, 标注在 字段(需要有 vo)上 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturn {
    /** 返回类型, 当需要自定义时(比如枚举)有用 */
    String type() default "";
    /** 返回说明 */
    String desc();
}
