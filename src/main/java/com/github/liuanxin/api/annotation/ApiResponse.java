package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponse {

    int code();

    String msg();
}
