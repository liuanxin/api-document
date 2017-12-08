package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口参数, 标注在方法参数或字段(当使用 dto 时)上 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParamIgnore {
}
