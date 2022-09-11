package com.github.liuanxin.api.annotation;

import com.github.liuanxin.api.web.DocumentController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @see com.github.liuanxin.api.model.DocumentCopyright
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DocumentController.class})
public @interface EnableApiInfo {
}
