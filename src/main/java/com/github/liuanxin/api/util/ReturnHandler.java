package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiReturn;
import com.github.liuanxin.api.annotation.ApiReturnIgnore;
import com.github.liuanxin.api.model.DocumentReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ReturnHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnHandler.class);
    private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    private static final String SPACE = " ";

    /** 返回结果的对象中用于泛型的类型: List&lt;T&gt;, List&lt;E&gt;, List&lt;A&gt;, Map&lt;K, V&gt; */
    @SuppressWarnings("unchecked")
    private static final List<String> GENERIC_CLASS_NAME = Tools.lists("T", "E", "A", "K", "V");

    /** 接口上的返回结果 */
    @SuppressWarnings("unchecked")
    public static List<DocumentReturn> handlerReturn(String method, boolean recordLevel) {
        String type = method.substring(method.indexOf(SPACE)).trim();
        type = type.substring(0, type.indexOf(SPACE)).trim();
        List<DocumentReturn> returnList = Tools.lists();
        handlerReturn(Tools.EMPTY, Tools.EMPTY, recordLevel, type, returnList);
        return returnList;
    }

    private static void handlerReturn(String space, String parent,
                                      boolean recordLevel, String type,
                                      List<DocumentReturn> returnList) {
        String clazz = type.contains("<") ? type.substring(0, type.indexOf("<")) : type;
        if ("void".equals(clazz)) {
            return;
        }
        Class<?> outClass = null;
        try {
            outClass = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(outClass)) {
            return;
        }
        if (outClass.isInterface()) {
            if (outClass == List.class) {
                if (type.contains("<") && type.contains(">")) {
                    String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    handlerReturn(space, parent, recordLevel, classType, returnList);
                }
            } else if (outClass == Map.class) {
                // map 尽量用实体类代替, 这样可以在实体类的字段上标注注解
                if (type.contains("<") && type.contains(">")) {
                    String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    String[] keyValue = keyAndValue.split(",");
                    if (keyValue.length == 2) {
                        // handlerReturn(space, parent, keyValue[0].trim(), returnList);
                        // just handler value
                        handlerReturn(space, parent, recordLevel, keyValue[1].trim(), returnList);
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("({})方法中 Map 的泛型有问题({})", type, keyAndValue);
                        }
                    }
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("不明确的接口类: {}", type);
                }
            }
        } else {
            // 非基础类型才需要进去获取字段
            if (Tools.notBasicType(outClass)) {
                Map<String, String> tmpFieldMap = Tools.newHashMap();
                for (Field field : outClass.getDeclaredFields()) {
                    int mod = field.getModifiers();
                    // 字段不是 static, 不是 final, 也没有标 ignore 注解
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                            && Tools.isBlank(field.getAnnotation(ApiReturnIgnore.class))) {
                        String fieldName = field.getName();
                        Class<?> fieldType = field.getType();

                        DocumentReturn documentReturn = new DocumentReturn().setName(space + fieldName + parent);
                        documentReturn.setType(Tools.getInputType(fieldType.getSimpleName()));

                        ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                        if (Tools.isNotBlank(apiReturn)) {
                            documentReturn.setDesc(apiReturn.desc());
                            String docType = apiReturn.type();
                            if (Tools.isNotBlank(docType)) {
                                documentReturn.setType(docType);
                            }
                        }
                        if (fieldType.isEnum()) {
                            // 如果是枚举, 则将自解释拼在说明中
                            String desc = documentReturn.getDesc();
                            String enumInfo = Tools.enumInfo(fieldType);
                            documentReturn.setDesc(Tools.isBlank(desc) ? enumInfo : (desc + "(" + enumInfo + ")"));
                        }
                        returnList.add(documentReturn);

                        // 如果返回字段不是基础数据类型则表示是一个类来接收的, 进去里面做一层
                        if (Tools.notBasicType(fieldType)) {
                            String innerType = field.getGenericType().toString();
                            String innerParent = recordLevel ? (" -> " + fieldName + parent) : Tools.EMPTY;
                            handlerReturn(space + TAB, innerParent, recordLevel, innerType, returnList);
                        }
                        tmpFieldMap.put(field.getGenericType().toString(), fieldName);
                    }
                }
                // 处理泛型里面的内容
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    String fieldName = handlerReturnFieldName(tmpFieldMap, innerType, recordLevel);
                    String innerParent = recordLevel ? (" -> " + fieldName + parent) : Tools.EMPTY;
                    handlerReturn(space + TAB, innerParent, recordLevel, innerType, returnList);
                }
            }
        }
    }

    private static String handlerReturnFieldName(Map<String, String> fieldMap, String innerType, boolean recordLevel) {
        String innerOutType = innerType.contains("<") ? innerType.substring(0, innerType.indexOf("<")) : innerType;
        if (!recordLevel) {
            return Tools.EMPTY;
        }
        String name = null;
        for (String className : GENERIC_CLASS_NAME) {
            name = fieldMap.get(className);
            if (Tools.isBlank(name)) {
                return name;
            }
        }
        if (Tools.isBlank(name)) {
            name = fieldMap.get(innerOutType);
        }
        if (Tools.isBlank(name)) {
            name = fieldMap.get(Object.class.getName());
        }
        return Tools.isBlank(name) ? Tools.EMPTY : name;
    }

    // ========== json ==========

    /** 处理结果 json */
    public static String handlerReturnJson(String method) {
        String type = method.substring(method.indexOf(SPACE)).trim();
        type = type.substring(0, type.indexOf(SPACE)).trim();
        Object obj = handlerReturnJsonObj(type);
        return Tools.isNotBlank(obj) ? Tools.toJson(obj) : Tools.EMPTY;
    }

    private static Object handlerReturnJsonObj(String type) {
        String clazz = type.contains("<") ? type.substring(0, type.indexOf("<")) : type;
        if ("void".equals(clazz)) {
            return null;
        }
        Class<?> outClass = null;
        try {
            outClass = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(outClass)) {
            return null;
        }
        if (outClass.isInterface()) {
            if (outClass == List.class) {
                return handlerReturnJsonList(type);
            } else if (outClass == Map.class) {
                return handlerReturnJsonMap(type);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("不明确的接口类: {}", type);
                }
            }
        } else {
            Object obj = handlerReturnWithObjClazz(outClass);
            if (Tools.isNotBlank(obj) && type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                handlerReturnJsonWithObj(outClass, innerType, obj);
            }
            return obj;
        }
        return null;
    }

    private static void handlerReturnJsonWithObj(Class<?> outClass, String type, Object obj) {
        String clazz = type.contains("<") ? type.substring(0, type.indexOf("<")) : type;
        if ("void".equals(clazz)) {
            return;
        }
        Class<?> innerClass = null;
        try {
            innerClass = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(innerClass)) {
            return;
        }
        if (innerClass.isInterface()) {
            if (innerClass == List.class) {
                setData(outClass, List.class, obj, handlerReturnJsonList(type));
            } else if (innerClass == Map.class) {
                setData(outClass, Map.class, obj, handlerReturnJsonMap(type));
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("不明确的接口类: {}", type);
                }
            }
        } else {
            Object value = handlerReturnWithObjClazz(innerClass);
            if (Tools.isNotBlank(value)) {
                setData(outClass, innerClass, obj, value);
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    handlerReturnJsonWithObj(innerClass, innerType, value);
                }
            }
        }
    }

    private static void setData(Class<?> clazz, Class<?> fieldClazz, Object obj, Object value) {
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            // 字段不是 static, 也不是 final
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                // 泛型在运行时已经被擦除了, 通过下面的方式获取的 getType() 总是 java.lang.Object
                // if (field.getType() == fieldClazz) { setField(field, obj, value); }
                Type type = field.getGenericType();
                if (GENERIC_CLASS_NAME.contains(type.toString())) {
                    setField(field, obj, value);
                } else if (type instanceof ParameterizedType) {
                    Type clazzType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (GENERIC_CLASS_NAME.contains(clazzType.toString()) || clazzType == fieldClazz) {
                        String listName = List.class.getName();
                        if (type.toString().startsWith(listName) || clazzType.toString().startsWith(listName)) {
                            setField(field, obj, Tools.lists(value));
                        } else {
                            setField(field, obj, value);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List handlerReturnJsonList(String type) {
        if (type.contains("<") && type.contains(">")) {
            String obj = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
            // 如果是 list, 加一条记录进去
            List list = Tools.lists();
            Object object = handlerReturnWithObj(obj);
            if (Tools.isNotBlank(object)) {
                list.add(object);
            }
            return list;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map handlerReturnJsonMap(String type) {
        if (type.contains("<") && type.contains(">")) {
            // 如果是 map, 加一条记录进去
            String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
            String[] keyValue = keyAndValue.split(",");
            if (keyValue.length == 2) {
                Map map = Tools.newLinkedHashMap();
                Object key = handlerReturnWithObj(keyValue[0]);
                Object value = handlerReturnWithObj(keyValue[1]);
                if (Tools.isNotBlank(key) && Tools.isNotBlank(value)) {
                    map.put(key, value);
                }
                return map;
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("({})方法中 Map 的泛型有问题({})", type, keyAndValue);
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Object handlerReturnWithObj(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(clazz)) {
            return null;
        }
        return handlerReturnWithObjClazz(clazz);
    }

    private static Object handlerReturnWithObjClazz(Class<?> clazz) {
        if (Tools.isBlank(clazz) || clazz == Object.class) {
            return null;
        }
        if (Tools.basicType(clazz)) {
            return getReturnType(clazz);
        }

        Object obj;
        try {
            obj = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(String.format("%s can't instance", clazz), e);
        }
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            // 字段不是 static, 不是 final, 也没有标 ignore 注解
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                    && Tools.isBlank(field.getAnnotation(ApiReturnIgnore.class))) {
                Class<?> type = field.getType();
                // 只在字符串的时候把注解上的值拿来用
                if (type == String.class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String value = Tools.EMPTY;
                    if (Tools.isNotBlank(apiReturn)) {
                        value = apiReturn.desc();
                    }
                    setField(field, obj, value);
                } else if (type == String[].class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String[] value = new String[] { Tools.EMPTY };
                    if (Tools.isNotBlank(apiReturn)) {
                        value = new String[] { apiReturn.desc() };
                    }
                    setField(field, obj, value);
                }
                else if (type.isEnum()) {
                    // 返回示例中的类型如果是枚举, 则拿第一个进行返回
                    Object value = null;
                    Object[] enumConstants = type.getEnumConstants();
                    if (enumConstants.length > 0) {
                        value = enumConstants[0];
                    }
                    setField(field, obj, value);
                }
                else if (type == List.class) {
                    setField(field, obj, handlerReturnJsonList(field.getGenericType().toString()));
                }
                else if (type == Map.class) {
                    setField(field, obj, handlerReturnJsonMap(field.getGenericType().toString()));
                }
                else if (Tools.basicType(type)) {
                    setField(field, obj, getReturnType(type));
                }
                else {
                    handlerReturnWithObj(field.getGenericType().toString());
                }
            }
        }
        return obj;
    }

    private static void setField(Field field, Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            // ignore
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("无法给 {} 对象的字段 {} 赋值 {}", obj, field, value);
            }
        }
    }

    private static Object getReturnType(Class<?> clazz) {
        if (clazz == boolean.class || clazz == Boolean.class) {
            return false;
        } else if (clazz == boolean[].class || clazz == Boolean[].class) {
            return new boolean[] { false };
        }

        else if (clazz == byte.class || clazz == Byte.class
                || clazz == char.class || clazz == Character.class
                || clazz == short.class || clazz == Short.class
                || clazz == int.class || clazz == Integer.class) {
            return 0;
        } else if (clazz == byte[].class || clazz == Byte[].class
                || clazz == char[].class || clazz == Character[].class
                || clazz == short[].class || clazz == Short[].class
                || clazz == int[].class || clazz == Integer[].class) {
            return new int[] { 0 };
        }

        else if (clazz == long.class || clazz == Long.class) {
            return 0L;
        } else if (clazz == long[].class || clazz == Long[].class) {
            return new long[] { 0L };
        }

        else if (clazz == float.class || clazz == Float.class) {
            return 0F;
        } else if (clazz == float[].class || clazz == Float[].class) {
            return new float[] { 0F };
        }

        else if (clazz == double.class || clazz == Double.class || clazz == BigDecimal.class) {
            return 0D;
        } else if (clazz == double[].class || clazz == Double[].class || clazz == BigDecimal[].class) {
            return new double[] { 0D };
        }

        else if (clazz == String.class) {
            return Tools.EMPTY;
        } else if (clazz == String[].class) {
            return new String[] { Tools.EMPTY };
        }

        else {
            return null;
        }
    }
}
