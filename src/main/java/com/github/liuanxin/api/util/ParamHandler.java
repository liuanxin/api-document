package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiParam;
import com.github.liuanxin.api.annotation.ApiParamIgnore;
import com.github.liuanxin.api.model.DocumentParam;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public final class ParamHandler {

    /** 如果使用 8 且在编译时打开了 javac -parameters 开关, 使用 parameter.getMethod().getParameters()[i].getName() 也可以获取 */
    private static final LocalVariableTableParameterNameDiscoverer VARIABLE = new LocalVariableTableParameterNameDiscoverer();

    /** 处理参数 */
    @SuppressWarnings("unchecked")
    public static List<DocumentParam> handlerParam(HandlerMethod handlerMethod) {
        List<DocumentParam> params = Tools.lists();
        int i = 0;
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            // 如果参数不是基础数据类型则表示是一个类来接收的, 进去里面做一层
            Class<?> parameterType = parameter.getParameterType();
            if (Tools.notBasicType(parameterType)) {
                for (Field field : parameterType.getDeclaredFields()) {
                    int mod= field.getModifiers();
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                        // 如果字段上标了 ignore 则忽略
                        if (Tools.isBlank(field.getAnnotation(ApiParamIgnore.class))) {
                            String paramName = field.getName();
                            params.add(paramInfo(paramName, field.getType(), field.getAnnotation(ApiParam.class)));
                        }
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
            i++;
        }
        return params;
    }

    /** 收集参数信息 */
    private static DocumentParam paramInfo(String name, Class<?> type, ApiParam apiParam) {
        DocumentParam param = new DocumentParam();
        param.setName(name);
        param.setType(Tools.getInputType(type));

        if (Tools.isNotBlank(apiParam)) {
            param.setMust(apiParam.must());
            // param.setExample(apiParam.example());
            param.setDesc(apiParam.desc());

            String paramName = apiParam.name();
            if (!Tools.EMPTY.equals(paramName)) {
                param.setName(paramName);
            }
            String paramType = apiParam.type();
            if (!Tools.EMPTY.equals(paramType)) {
                param.setType(apiParam.type());
            }
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
