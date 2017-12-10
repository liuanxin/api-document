package com.github.liuanxin.api.configuration;

import com.github.liuanxin.api.web.DocumentController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = DocumentController.class)
public class ApiInfoConfiguration {
}
