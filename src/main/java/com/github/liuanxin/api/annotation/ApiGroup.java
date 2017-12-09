package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** 接口组, 标注在 类 或 方法 上 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiGroup {
    /** 模块名, name-中文说明(如: user-用户). 一个接口必须隶属于某个模块 */
    String[] value();
}
