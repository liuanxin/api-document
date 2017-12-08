package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口返回结果, 标注在 字段(需要有 vo)上 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturnIgnore {
}
