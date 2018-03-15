package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/** want to ignore some param, use this */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParamIgnore {
}
