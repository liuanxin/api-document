package com.github.liuanxin.api.util;

import com.github.liuanxin.api.model.DocumentParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
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

    @Test
    public void shouldRecognizeRequestHeader() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("header", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        List<DocumentParam> params = ParamHandler.handlerParam(handlerMethod);

        Assertions.assertEquals(1, params.size());
        Assertions.assertEquals("Authorization", params.get(0).getName());
        Assertions.assertEquals("1", params.get(0).getParamType());
    }

    @Test
    public void shouldIgnoreServerSideAndCookieParams() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("serverParams", String.class, String.class, String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        List<DocumentParam> params = ParamHandler.handlerParam(handlerMethod);

        Assertions.assertTrue(params.isEmpty());
    }

    public static class TestController {

        public void query(@RequestParam("keyword") String value) {
        }

        public void header(@RequestHeader("Authorization") String authorization) {
        }

        public void serverParams(@RequestAttribute("requestData") String requestData,
                                 @SessionAttribute("sessionData") String sessionData,
                                 @CookieValue("token") String token) {
        }
    }
}
