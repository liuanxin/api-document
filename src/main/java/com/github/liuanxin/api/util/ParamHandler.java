package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiModel;
import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.MethodParameter;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
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

    // spring 5 : new LocalVariableTableParameterNameDiscoverer()
    // private static final LocalVariableTableParameterNameDiscoverer VARIABLE = new LocalVariableTableParameterNameDiscoverer();

    // spring 6 : new StandardReflectionParameterNameDiscoverer()
    /** https://github.com/spring-projects/spring-framework/issues/29559 */
    private static final StandardReflectionParameterNameDiscoverer VARIABLE = new StandardReflectionParameterNameDiscoverer();

    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = new LinkedList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            if (Tools.isNotNull(parameter) && Tools.isNull(parameter.getParameterAnnotation(ApiParamIgnore.class))) {
                // if param not basicType, into a layer of processing
                Class<?> parameterType = parameter.getParameterType();
                ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
                ApiModel apiModel = parameter.getParameterAnnotation(ApiModel.class);
                String showDataType = Tools.isNotNull(apiParam) ? apiParam.dataType()
                        : (Tools.isNotNull(apiModel) ? apiModel.dataType() : null);

                if (Tools.hasInDepth(showDataType, parameterType)) {
                    for (Field field : parameterType.getDeclaredFields()) {
                        int mod = field.getModifiers();
                        // field not static, not final, not annotation ignore
                        if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                                && Tools.isNull(field.getAnnotation(ApiParamIgnore.class))) {
                            ApiParam fieldParam = field.getAnnotation(ApiParam.class);
                            ApiModel fieldModel = field.getAnnotation(ApiModel.class);
                            params.add(paramInfo(field.getName(), field.getType(), fieldParam, fieldModel, false));
                        }
                    }
                } else {
                    // The variable name is erased when compiled by jvm, parameter.parameterName() is null
                    // When use java 8 and open options in javac -parameters, parameter.parameterName() can be get
                    // String paramName = parameter.getParameterName();
                    Method method = parameter.getMethod();
                    if (Tools.isNotNull(method)) {
                        String[] sourceParamName = VARIABLE.getParameterNames(method);
                        if (Tools.isNotNull(sourceParamName) && sourceParamName.length > i) {
                            // if param was required, use it.
                            params.add(paramInfo(getParamName(parameter, sourceParamName[i]), parameterType,
                                    apiParam, apiModel, paramRequired(parameter)));
                        }
                    }
                }
            }
        }
        return params;
    }

    private static String getParamName(MethodParameter parameter, String paramName) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (Tools.isNotNull(requestParam) && Tools.isNotEmpty(requestParam.value())) {
            return requestParam.value();
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (Tools.isNotNull(pathVariable) && Tools.isNotEmpty(pathVariable.value())) {
            return pathVariable.value();
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (Tools.isNotNull(requestAttribute) && Tools.isNotEmpty(requestAttribute.value())) {
            return requestAttribute.value();
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (Tools.isNotNull(requestHeader) && Tools.isNotEmpty(requestHeader.value())) {
            return requestHeader.value();
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (Tools.isNotNull(requestPart) && Tools.isNotEmpty(requestPart.value())) {
            return requestPart.value();
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (Tools.isNotNull(sessionAttribute) && Tools.isNotEmpty(sessionAttribute.value())) {
            return sessionAttribute.value();
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (Tools.isNotNull(matrixVariable) && Tools.isNotEmpty(matrixVariable.value())) {
            return matrixVariable.value();
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (Tools.isNotNull(cookieValue) && Tools.isNotEmpty(cookieValue.value())) {
            return cookieValue.value();
        }
        return paramName;
    }

    private static boolean paramRequired(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (Tools.isNotNull(requestParam) && requestParam.required()) {
            return true;
        }

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (Tools.isNotNull(pathVariable) && pathVariable.required()) {
            return true;
        }

        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        if (Tools.isNotNull(requestBody) && requestBody.required()) {
            return true;
        }

        RequestAttribute requestAttribute = parameter.getParameterAnnotation(RequestAttribute.class);
        if (Tools.isNotNull(requestAttribute) && requestAttribute.required()) {
            return true;
        }

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (Tools.isNotNull(requestHeader) && requestHeader.required()) {
            return true;
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (Tools.isNotNull(requestPart) && requestPart.required()) {
            return true;
        }

        SessionAttribute sessionAttribute = parameter.getParameterAnnotation(SessionAttribute.class);
        if (Tools.isNotNull(sessionAttribute) && sessionAttribute.required()) {
            return true;
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (Tools.isNotNull(matrixVariable) && matrixVariable.required()) {
            return true;
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        return Tools.isNotNull(cookieValue) && cookieValue.required();
    }

    /** collect param info */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam, ApiModel apiModel, boolean required) {
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

            String example = Tools.escape(apiParam.example());
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

            String example = Tools.escape(apiModel.example());
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
        param.setRequired(
                required
                || (Tools.isNotEmpty(apiParam) && apiParam.required())
                || (Tools.isNotEmpty(apiModel) && apiModel.required()) ? "1" : ApiConst.EMPTY
        );
        param.setDesc(Tools.descInfo(type, desc));
        return param;
    }
}
