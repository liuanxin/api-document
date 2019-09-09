package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public final class ParamHandler {

    private static final LocalVariableTableParameterNameDiscoverer VARIABLE
            = new LocalVariableTableParameterNameDiscoverer();

    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = new LinkedList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            if (Tools.isBlank(parameter.getParameterAnnotation(ApiParamIgnore.class))) {
                // if param not basicType, into a layer of processing
                Class<?> parameterType = parameter.getParameterType();
                if (!parameterType.equals(MultipartFile.class) && Tools.notBasicType(parameterType)) {
                    for (Field field : parameterType.getDeclaredFields()) {
                        int mod = field.getModifiers();
                        // field not static, not final, not annotation ignore
                        if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                                && Tools.isBlank(field.getAnnotation(ApiParamIgnore.class))) {
                            ApiParam apiParam = field.getAnnotation(ApiParam.class);
                            params.add(paramInfo(field.getName(), field.getType(), apiParam, false));
                        }
                    }
                } else {
                    // The variable name is erased when compiled by jvm, parameter.parameterName() is null
                    // if use java 8 and open options in javac -parameters, parameter.parameterName() can be get
                    // String paramName = parameter.getParameterName();
                    String paramName = VARIABLE.getParameterNames(parameter.getMethod())[i];
                    ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
                    // if param was required, use it.
                    params.add(paramInfo(getParamName(parameter, paramName), parameterType, apiParam, paramHasMust(parameter)));
                }
            }
        }
        return params;
    }

    private static String getParamName(MethodParameter parameter, String paramName) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && Tools.isNotEmpty(requestParam.value())) {
            return requestParam.value();
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && Tools.isNotEmpty(pathVariable.value())) {
            return pathVariable.value();
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (requestAttribute != null && Tools.isNotEmpty(requestAttribute.value())) {
            return requestAttribute.value();
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null && Tools.isNotEmpty(requestHeader.value())) {
            return requestHeader.value();
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (requestPart != null && Tools.isNotEmpty(requestPart.value())) {
            return requestPart.value();
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (sessionAttribute != null && Tools.isNotEmpty(sessionAttribute.value())) {
            return sessionAttribute.value();
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (matrixVariable != null && Tools.isNotEmpty(matrixVariable.value())) {
            return matrixVariable.value();
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null && Tools.isNotEmpty(cookieValue.value())) {
            return cookieValue.value();
        }
        return paramName;
    }

    private static boolean paramHasMust(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && requestParam.required()) {
            return true;
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && pathVariable.required()) {
            return true;
        }

        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        if (requestBody != null && requestBody.required()) {
            return true;
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (requestAttribute != null && requestAttribute.required()) {
            return true;
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null && requestHeader.required()) {
            return true;
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (requestPart != null && requestPart.required()) {
            return true;
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (sessionAttribute != null && sessionAttribute.required()) {
            return true;
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (matrixVariable != null && matrixVariable.required()) {
            return true;
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        return cookieValue != null && cookieValue.required();
    }

    /** collect param info */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam, boolean must) {
        DocumentParam param = new DocumentParam();
        param.setName(name);

        String inputType = Tools.getInputType(type);
        param.setDataType(inputType);
        param.setHasFile(Tools.hasFileInput(inputType));

        String desc;
        if (Tools.isNotEmpty(apiParam)) {
            desc = apiParam.value();

            String paramName = apiParam.name();
            if (Tools.isNotEmpty(paramName)) {
                param.setName(paramName);
            }
            String showDataType = apiParam.dataType();
            if (Tools.isNotEmpty(showDataType)) {
                param.setShowDataType(showDataType);
            }

            param.setParamType(apiParam.paramType().toString());

            String example = apiParam.example();
            if (Tools.isNotEmpty(example)) {
                param.setExample(example);
            }
            param.setHasTextarea(apiParam.textarea());
            param.setStyle(apiParam.style());
        } else {
            desc = Tools.EMPTY;
        }
        // if param has no @RequestParam(required = true) etc..., use custom value
        param.setMust(must || (Tools.isNotEmpty(apiParam) && apiParam.must()));
        param.setDesc(Tools.descInfo(type, desc));
        return param;
    }
}
