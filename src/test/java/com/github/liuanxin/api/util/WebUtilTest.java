package com.github.liuanxin.api.util;

import com.github.liuanxin.api.model.DocumentCopyright;
import com.github.liuanxin.api.model.DocumentInfoAndUrlMap;
import com.github.liuanxin.api.model.DocumentUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.util.Map;

public class WebUtilTest {

    @Test
    public void shouldCollectPathPatternUrl() throws NoSuchMethodException {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths("/users/{id}").options(options).build();
        TestController controller = new TestController();
        Method method = TestController.class.getMethod("detail");
        mapping.registerMapping(mappingInfo, controller, method);

        DocumentInfoAndUrlMap result = WebUtil.infoAndUrlMap(mapping, new DocumentCopyright());

        Map<String, DocumentUrl> documentMap = result.getDocumentMap();
        Assertions.assertEquals(1, documentMap.size());
        DocumentUrl document = documentMap.values().iterator().next();
        Assertions.assertEquals("/users/{id}", document.getUrl());
        Assertions.assertEquals("/api/example/" + document.getId() + ".json", document.getExampleUrl());
    }

    @RestController
    public static class TestController {

        public String detail() {
            return "ok";
        }
    }
}
