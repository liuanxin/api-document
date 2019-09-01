package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiMethod {

    /** @return method title */
    String value();

    /** @return api developer */
    String develop() default "";

    /** @return api description */
    String desc() default "";

    /** @return more forward when smaller */
    int index() default Integer.MAX_VALUE;

    /**
     * true  : output in the return example.
     * false : listed separately.
     * @return return whether the sample contains a comment
     */
    boolean[] commentInReturnExample() default {};

    /**
     * true  : the hierarchical relationship corresponding to the field will be used to process the comment, Generics be sure to use certain types.
     * false : will use index corresponding to the field to process the comment, when use @JsonPropertyOrder to change field sort, there will be problems.
     * @return use default
     */
    boolean commentInReturnExampleWithLevel() default true;

    /**
     * Return Field Description Whether to record the parent attribute when listed separately,
     * regardless of this value when commentInReturnExample is true.
     *
     * forget this. It's a bad ide
     */
    boolean[] returnRecordLevel() default {};

    /**
     * customize return type, if set, will ignore 「the return type」 on method
     */
    ApiReturnType[] returnType() default {};
}
