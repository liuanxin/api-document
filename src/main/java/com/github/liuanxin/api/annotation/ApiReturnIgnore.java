package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 不让接口返回字段出现在文档中, 标注在 字段(需要有 vo)上 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturnIgnore {
}
