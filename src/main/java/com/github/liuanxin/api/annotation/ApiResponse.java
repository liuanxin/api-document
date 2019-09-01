package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponse {

    int code();

    String msg();

    ApiReturnType[] type() default {};
}
