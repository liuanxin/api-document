package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiMethod;
import com.github.liuanxin.api.annotation.ApiReturnType;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.DocumentResponse;
import org.springframework.web.method.HandlerMethod;

public class ReturnType {
    static String getRequestBodyParamTypeByMethod(HandlerMethod handlerMethod) {
        Class<?>[] parameterTypes = handlerMethod.getMethod().getParameterTypes();
        return Tools.isNotEmpty(parameterTypes) ? parameterTypes[0].getName() : ApiConst.EMPTY;
    }

    static String getReturnTypeByMethod(HandlerMethod handlerMethod, ApiMethod apiMethod) {
        String returnType;
        if (Tools.isNotBlank(apiMethod)) {
            returnType = getReturnTypeByAnnotation(Tools.first(apiMethod.returnType()));
        } else {
            returnType = ApiConst.EMPTY;
        }

        if (Tools.isEmpty(returnType) && Tools.isNotBlank(handlerMethod)) {
            returnType = handlerMethod.getMethod().getGenericReturnType().toString();
        }
        if (Tools.isNotBlank(returnType)) {
            String prefix = "class ";
            if (returnType.startsWith(prefix)) {
                returnType = returnType.substring(prefix.length());
            }
        }
        return returnType;
    }

    static String getReturnTypeByAnnotation(ApiReturnType type) {
        return Tools.isBlank(type)
                ? null
                : getReturnType(type.value(), type.genericParent(), type.generic(), type.genericChild());
    }

    private static String getReturnType(Class<?> response, Class<?> genericParent, Class<?>[] generic, Class<?>[] genericChild) {
        if (Tools.isBlank(response)) {
            return null;
        } else {
            StringBuilder sbd = new StringBuilder();
            sbd.append(response.getName());
            if (Tools.isNotBlank(genericParent) && genericParent != Void.class) {
                sbd.append("<").append(genericParent.getName());
            }

            if (Tools.isNotBlank(generic)) {
                int secondLen = generic.length;
                if (secondLen > 0) {
                    int childrenLen = 0;
                    if (Tools.isNotBlank(genericChild)) {
                        childrenLen = genericChild.length;
                        if (childrenLen > 0 && secondLen > 1) {
                            secondLen = 1;
                        }
                    }

                    sbd.append("<");
                    for (int i = 0; i < secondLen; i++) {
                        if (i > 0) {
                            sbd.append(", ");
                        }
                        sbd.append(generic[i].getName());
                    }
                    if (childrenLen > 0) {
                        sbd.append("<");
                        for (int i = 0; i < childrenLen; i++) {
                            if (i > 0) {
                                sbd.append(", ");
                            }
                            sbd.append(genericChild[i].getName());
                        }
                        sbd.append(">");
                    }
                    sbd.append(">");
                }
            }

            if (Tools.isNotBlank(genericParent) && genericParent != Void.class) {
                sbd.append(">");
            }
            return sbd.toString();
        }
    }

    static String getReturnTypeByResponse(DocumentResponse res) {
        return Tools.isBlank(res) || Tools.isBlank(res.getResponse())
                ? null
                : getReturnType(res.getResponse(), res.getGenericParent(), res.getGeneric(), res.getGenericChild());
    }
}
