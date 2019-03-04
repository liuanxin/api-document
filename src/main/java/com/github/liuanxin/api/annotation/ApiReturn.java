package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturn {

    /** return comment */
    String value();

    /** when customize type comment, use this(example for Enum) */
    String type() default "";

    /** return example, only if the field is a string or underlying data type(including BigInteger and BigDecimal) */
    String example() default "";
}
