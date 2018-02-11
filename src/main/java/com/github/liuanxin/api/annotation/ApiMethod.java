package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口方法, 标注在 方法 上 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiMethod {
    /** 接口标题 */
    String title();
    /** 接口详细说明 */
    String desc() default "";
    /** 开发者及联系方式 */
    String develop();

    /** 排序, 越小越靠前 */
    int index() default Integer.MAX_VALUE;
}
