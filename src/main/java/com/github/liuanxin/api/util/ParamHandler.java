package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class ParamHandler {

    private static final LocalVariableTableParameterNameDiscoverer VARIABLE
            = new LocalVariableTableParameterNameDiscoverer();

    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = new ArrayList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            // if param not basicType, into a layer of processing
            Class<?> parameterType = parameter.getParameterType();
            if (Tools.notBasicType(parameterType)) {
                for (Field field : parameterType.getDeclaredFields()) {
                    int mod= field.getModifiers();
                    // field not static, not final, not annotation ignore
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                            && Tools.isBlank(field.getAnnotation(ApiParamIgnore.class))) {
                        ApiParam apiParam = field.getAnnotation(ApiParam.class);
                        params.add(paramInfo(field.getName(), field.getType(), apiParam, false));
                    }
                }
            } else {
                if (Tools.isBlank(parameter.getParameterAnnotation(ApiParamIgnore.class))) {
                    // The variable name is erased when compiled by jvm, parameter.parameterName() is null
                    // if use java 8 and open options in javac -parameters, parameter.parameterName() can be get
                    // String paramName = parameter.getParameterName();
                    String paramName = VARIABLE.getParameterNames(parameter.getMethod())[i];
                    ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
                    // if param was required, use it.
                    params.add(paramInfo(paramName, parameterType, apiParam, paramHasMust(parameter)));
                }
            }
        }
        return params;
    }

    /** 参数必须就返回 true */
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
        if (cookieValue != null && cookieValue.required()) {
            return true;
        }
        // else
        return false;
    }

    /** collect param info */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam, boolean must) {
        DocumentParam param = new DocumentParam();
        param.setName(name);
        param.setDataType(Tools.getInputType(type));

        if (Tools.isNotBlank(apiParam)) {
            String desc = apiParam.value();
            if (Tools.isNotBlank(desc)) {
                param.setDesc(desc);
            }

            String paramName = apiParam.name();
            if (Tools.isNotBlank(paramName)) {
                param.setName(paramName);
            }
            String dataType = apiParam.dataType();
            if (Tools.isNotBlank(dataType)) {
                param.setDataType(apiParam.dataType());
            }

            param.setParamType(apiParam.paramType().toString());

            String example = apiParam.example();
            if (Tools.isNotBlank(example)) {
                param.setExample(example);
            }
            // if param has no @RequestParam(required = true) etc..., use custom value
            param.setMust(must || apiParam.must());
            param.setHasTextarea(apiParam.textarea());
        }
        if (type.isEnum()) {
            // enum append (code:value)
            String desc = param.getDesc();
            String enumInfo = Tools.enumInfo(type);
            param.setDesc(Tools.isBlank(desc) ? enumInfo : (desc + "(" + enumInfo + ")"));
        }
        return param;
    }
}
