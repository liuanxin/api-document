package com.github.liuanxin.api.util;

import com.github.liuanxin.api.model.DocumentParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

public class ParamHandlerTest {

    @Test
    public void shouldUseExplicitRequestParamName() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("query", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        List<DocumentParam> params = ParamHandler.handlerParam(handlerMethod);

        Assertions.assertEquals(1, params.size());
        Assertions.assertEquals("keyword", params.get(0).getName());
    }

    public static class TestController {

        public void query(@RequestParam("keyword") String value) {
        }
    }
}
