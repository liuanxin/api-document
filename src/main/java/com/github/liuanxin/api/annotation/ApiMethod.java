package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiMethod {

    String value();

    String develop() default "";

    String desc() default "";

    /** more forward when smaller */
    int index() default Integer.MAX_VALUE;

    /** return whether the sample contains a comment */
    boolean commentInReturnExample() default true;

    /**
     * true  : the hierarchical relationship corresponding to the field will be used to process the comment, Generics be sure to use certain types.
     * false : will use index corresponding to the field to process the comment, when use @JsonPropertyOrder to change field sort, there will be problems.
     */
    boolean commentInReturnExampleWithLevel() default true;

    boolean returnRecordLevel() default false;
}
