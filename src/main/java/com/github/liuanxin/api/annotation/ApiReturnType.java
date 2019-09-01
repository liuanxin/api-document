package com.github.liuanxin.api.annotation;

import java.lang.annotation.*;

/**
 * <pre>
 * Vo ==> @ApiReturnType(Vo.class)
 *
 * List&lt;Vo&gt;        ==> @ApiReturnType(value = List.class, generic = Vo.class)
 * Set&lt;Vo&gt;         ==> @ApiReturnType(value = Set.class,  generic = Vo.class)
 * Map&lt;String, Vo&gt; ==> @ApiReturnType(value = Map.class,  generic = { String.class, Vo.class })
 * JsonResult&lt;Vo&gt;  ==> @ApiReturnType(value = JsonResult.class, generic = Vo.class)
 *
 * JsonResult&lt;List&lt;Vo&gt;&gt;        ==> @ApiReturnType(value = JsonResult.class, genericParent = List.class, generic = Vo.class)
 * JsonResult&lt;Set&lt;Vo&gt;&gt;         ==> @ApiReturnType(value = JsonResult.class, genericParent = Set.class,  generic = Vo.class)
 * JsonResult&lt;Map&lt;String, Vo&gt;&gt; ==> @ApiReturnType(value = JsonResult.class, genericParent = Map.class,  generic = { String.class, Vo.class })
 * JsonResult&lt;Xxx&lt;Vo&gt;&gt;         ==> @ApiReturnType(value = JsonResult.class, genericParent = Xxx.class, generic = Vo.class)
 *
 * JsonResult&lt;Xxx&lt;List&lt;Vo&gt;&gt;&gt;        ==> @ApiReturnType(value = JsonResult.class, genericParent = Xxx.class, generic = List.class, genericChild = Vo.class)
 * JsonResult&lt;Xxx&lt;Set&lt;Vo&gt;&gt;&gt;         ==> @ApiReturnType(value = JsonResult.class, genericParent = Xxx.class, generic = Set.class,  genericChild = Vo.class)
 * JsonResult&lt;Xxx&lt;Map&lt;String, Vo&gt;&gt;&gt; ==> @ApiReturnType(value = JsonResult.class, genericParent = Xxx.class, generic = Map.class,  genericChild = { String.class, Vo.class })
 *
 * <span style="color:red;">Now that only three layers have been abstracted, there are no good ways to abstract more layers.</span>
 * </pre>
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiReturnType {

    Class<?> value();

    Class<?> firstGeneric() default Void.class;

    Class<?>[] secondGeneric() default {};

    Class<?>[] thirdGeneric() default {};
}
