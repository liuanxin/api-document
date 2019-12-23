package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiModel;
import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public final class ParamHandler {

    private static final LocalVariableTableParameterNameDiscoverer VARIABLE
            = new LocalVariableTableParameterNameDiscoverer();

    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = new LinkedList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            if (Tools.isNotBlank(parameter) && Tools.isBlank(parameter.getParameterAnnotation(ApiParamIgnore.class))) {
                // if param not basicType, into a layer of processing
                Class<?> parameterType = parameter.getParameterType();
                ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
                ApiModel apiModel = parameter.getParameterAnnotation(ApiModel.class);
                String showDataType = Tools.isNotBlank(apiParam)
                        ? apiParam.dataType()
                        : (Tools.isNotBlank(apiModel) ? apiModel.dataType() : null);

                if (Tools.hasInDepth(showDataType, parameterType)) {
                    for (Field field : parameterType.getDeclaredFields()) {
                        int mod = field.getModifiers();
                        // field not static, not final, not annotation ignore
                        if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                                && Tools.isBlank(field.getAnnotation(ApiParamIgnore.class))) {
                            ApiParam fieldParam = field.getAnnotation(ApiParam.class);
                            ApiModel fieldModel = field.getAnnotation(ApiModel.class);
                            params.add(paramInfo(field.getName(), field.getType(), fieldParam, fieldModel, false));
                        }
                    }
                } else {
                    // The variable name is erased when compiled by jvm, parameter.parameterName() is null
                    // if use java 8 and open options in javac -parameters, parameter.parameterName() can be get
                    // String paramName = parameter.getParameterName();
                    Method method = parameter.getMethod();
                    if (Tools.isNotBlank(method)) {
                        String[] parameterNames = VARIABLE.getParameterNames(method);
                        if (Tools.isNotBlank(parameterNames) && parameterNames.length > i) {
                            // if param was required, use it.
                            params.add(paramInfo(getParamName(parameter, parameterNames[i]), parameterType,
                                    apiParam, apiModel, paramHasMust(parameter)));
                        }
                    }
                }
            }
        }
        return params;
    }

    private static String getParamName(MethodParameter parameter, String paramName) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (Tools.isNotBlank(requestParam) && Tools.isNotEmpty(requestParam.value())) {
            return requestParam.value();
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (Tools.isNotBlank(pathVariable) && Tools.isNotEmpty(pathVariable.value())) {
            return pathVariable.value();
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (Tools.isNotBlank(requestAttribute) && Tools.isNotEmpty(requestAttribute.value())) {
            return requestAttribute.value();
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (Tools.isNotBlank(requestHeader) && Tools.isNotEmpty(requestHeader.value())) {
            return requestHeader.value();
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (Tools.isNotBlank(requestPart) && Tools.isNotEmpty(requestPart.value())) {
            return requestPart.value();
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (Tools.isNotBlank(sessionAttribute) && Tools.isNotEmpty(sessionAttribute.value())) {
            return sessionAttribute.value();
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (Tools.isNotBlank(matrixVariable) && Tools.isNotEmpty(matrixVariable.value())) {
            return matrixVariable.value();
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (Tools.isNotBlank(cookieValue) && Tools.isNotEmpty(cookieValue.value())) {
            return cookieValue.value();
        }
        return paramName;
    }

    private static boolean paramHasMust(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (Tools.isNotBlank(requestParam) && requestParam.required()) {
            return true;
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (Tools.isNotBlank(pathVariable) && pathVariable.required()) {
            return true;
        }

        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        if (Tools.isNotBlank(requestBody) && requestBody.required()) {
            return true;
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (Tools.isNotBlank(requestAttribute) && requestAttribute.required()) {
            return true;
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (Tools.isNotBlank(requestHeader) && requestHeader.required()) {
            return true;
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (Tools.isNotBlank(requestPart) && requestPart.required()) {
            return true;
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (Tools.isNotBlank(sessionAttribute) && sessionAttribute.required()) {
            return true;
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (Tools.isNotBlank(matrixVariable) && matrixVariable.required()) {
            return true;
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        return Tools.isNotBlank(cookieValue) && cookieValue.required();
    }

    /** collect param info */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam,
                                           ApiModel apiModel, boolean must) {
        DocumentParam param = new DocumentParam();
        param.setName(name);

        String inputType = Tools.getInputType(type);
        param.setDataType(inputType);
        param.setHasFile(Tools.hasFileInput(inputType) ? "1" : ApiConst.EMPTY);

        String desc;
        if (Tools.isNotEmpty(apiParam)) {
            desc = apiParam.value();
            String datePattern = apiParam.datePattern();
            if (Tools.isNotEmpty(datePattern) &&
                    (Date.class.isAssignableFrom(type) || ApiConst.DATES.contains(apiParam.dataType().toLowerCase()))) {
                desc = Tools.isEmpty(desc) ? datePattern : desc + "(" + datePattern + ")";
            }

            String paramName = apiParam.name();
            if (Tools.isNotEmpty(paramName)) {
                param.setName(paramName);
            }
            String showDataType = apiParam.dataType();
            if (Tools.isNotEmpty(showDataType)) {
                param.setShowDataType(showDataType);
            }

            param.setParamType(apiParam.paramType().hasHeader() ? "1" : ApiConst.EMPTY);

            String example = apiParam.example();
            if (Tools.isNotEmpty(example)) {
                param.setExample(example);
            }
            param.setHasTextarea(apiParam.textarea() ? "1" : ApiConst.EMPTY);
            param.setDatePattern(datePattern);
            param.setStyle(apiParam.style());
        } else if (Tools.isNotEmpty(apiModel)) {
            desc = apiModel.value();
            String datePattern = apiModel.datePattern();
            if (Tools.isNotEmpty(datePattern) &&
                    (Date.class.isAssignableFrom(type) || ApiConst.DATES.contains(apiModel.dataType().toLowerCase()))) {
                desc = Tools.isEmpty(desc) ? datePattern : desc + "(" + datePattern + ")";
            }

            String paramName = apiModel.name();
            if (Tools.isNotEmpty(paramName)) {
                param.setName(paramName);
            }
            String showDataType = apiModel.dataType();
            if (Tools.isNotEmpty(showDataType)) {
                param.setShowDataType(showDataType);
            }

            param.setParamType(apiModel.paramType().hasHeader() ? "1" : ApiConst.EMPTY);

            String example = apiModel.example();
            if (Tools.isNotEmpty(example)) {
                param.setExample(example);
            }
            param.setHasTextarea(apiModel.textarea() ? "1" : ApiConst.EMPTY);
            param.setDatePattern(datePattern);
            param.setStyle(apiModel.style());
        } else {
            desc = ApiConst.EMPTY;
        }

        // if param has no @RequestParam(required = true) etc..., use custom value
        param.setHasMust(
                must
                || (Tools.isNotEmpty(apiParam) && apiParam.must())
                || (Tools.isNotEmpty(apiModel) && apiModel.must()) ? "1" : ApiConst.EMPTY
        );
        param.setDesc(Tools.descInfo(type, desc));
        return param;
    }
}
