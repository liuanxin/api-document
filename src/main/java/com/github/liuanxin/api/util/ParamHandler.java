package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class ParamHandler {

    /** 如果使用 8 且在编译时打开了 javac -parameters 开关, 使用 parameter.getMethod().getParameters()[i].getName() 也可以获取 */
    private static final LocalVariableTableParameterNameDiscoverer VARIABLE = new LocalVariableTableParameterNameDiscoverer();

    /** 处理参数 */
    @SuppressWarnings("unchecked")
    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = new ArrayList<>();
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            // 如果参数不是基础数据类型则表示是一个类来接收的, 进去里面做一层
            Class<?> parameterType = parameter.getParameterType();
            if (Tools.notBasicType(parameterType)) {
                for (Field field : parameterType.getDeclaredFields()) {
                    int mod= field.getModifiers();
                    // 字段不是 static, 不是 final, 也没有标 ignore 注解
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                            && Tools.isBlank(field.getAnnotation(ApiParamIgnore.class))) {
                        String paramName = field.getName();
                        params.add(paramInfo(paramName, field.getType(), field.getAnnotation(ApiParam.class)));
                    }
                }
            } else {
                if (Tools.isBlank(parameter.getParameterAnnotation(ApiParamIgnore.class))) {
                    // 受 jvm 编译时会擦除变量名的限制, parameter.parameterName 得到的都是 null 值
                    // String paramName = parameter.getParameterName();
                    String paramName = VARIABLE.getParameterNames(parameter.getMethod())[i];
                    params.add(paramInfo(paramName, parameterType, parameter.getParameterAnnotation(ApiParam.class)));
                }
            }
        }
        return params;
    }

    /** 收集参数信息 */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam) {
        DocumentParam param = new DocumentParam();
        param.setName(name);
        param.setDataType(Tools.getInputType(type));

        if (Tools.isNotBlank(apiParam)) {
            String desc = apiParam.desc();
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

            param.setMust(apiParam.must());
        }
        if (type.isEnum()) {
            // 如果是枚举, 则将自解释拼在说明中
            String desc = param.getDesc();
            String enumInfo = Tools.enumInfo(type);
            param.setDesc(Tools.isBlank(desc) ? enumInfo : (desc + "(" + enumInfo + ")"));
        }
        return param;
    }
}
