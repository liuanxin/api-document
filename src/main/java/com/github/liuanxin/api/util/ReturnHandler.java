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

    /** 返回结果的对象中用于泛型的类型 */
    @SuppressWarnings("unchecked")
    private static final List<String> GENERIC_CLASS_NAME = Utils.lists("T");

    /** 接口上的返回结果 */
    @SuppressWarnings("unchecked")
    public static List<DocumentReturn> handlerReturn(String method) {
        String type = method.substring(method.indexOf(" ")).trim();
        type = type.substring(0, type.indexOf(" ")).trim();
        List<DocumentReturn> returnList = Utils.lists();
        handlerReturn("", "", type, returnList);
        return returnList;
    }
    private static void handlerReturn(String space, String parent, String type, List<DocumentReturn> returnList) {
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
        if (outClass == null) {
            return;
        }
        if (outClass.isInterface()) {
            if (outClass == List.class) {
                if (type.contains("<") && type.contains(">")) {
                    String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    handlerReturn(space, parent, classType, returnList);
                }
            } else if (outClass == Map.class) {
                // map 尽量用实体类代替, 这样可以在实体类的字段上标注注解
                if (type.contains("<") && type.contains(">")) {
                    String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    String[] keyValue = keyAndValue.split(",");
                    if (keyValue.length == 2) {
                        // handlerReturn(space, parent, keyValue[0].trim(), returnList);
                        // just handler value
                        handlerReturn(space, parent, keyValue[1].trim(), returnList);
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
            if (Utils.notBasicType(outClass)) {
                for (Field field : outClass.getDeclaredFields()) {
                    int mod = field.getModifiers();
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                            && field.getAnnotation(ApiReturnIgnore.class) == null) {
                        String fieldName = field.getName();
                        DocumentReturn documentReturn = new DocumentReturn().setName(space + fieldName + parent);
                        documentReturn.setType(Utils.getInputType(field.getType().getSimpleName()));

                        ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                        if (apiReturn != null) {
                            documentReturn.setDesc(apiReturn.desc());
                            String docType = apiReturn.type();
                            if (!"".equals(docType)) {
                                documentReturn.setType(docType);
                            }
                        }
                        returnList.add(documentReturn);

                        // 如果返回字段不是基础数据类型则表示是一个类来接收的, 进去里面做一层
                        if (Utils.notBasicType(field.getType())) {
                            String innerType = field.getGenericType().toString();
                            String innerParent = "";//" -> " + fieldName + parent;
                            handlerReturn(space + "&nbsp;&nbsp;&nbsp;&nbsp;", innerParent, innerType, returnList);
                        }
                    }
                }
                // 处理泛型里面的内容
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    String innerParent = "";//" -> " + parent;
                    handlerReturn(space + "&nbsp;&nbsp;&nbsp;&nbsp;", innerParent, innerType, returnList);
                }
            }
        }
    }

    // ========== json ==========

    /** 处理结果 json */
    public static String handlerReturnJson(String method) {
        String type = method.substring(method.indexOf(" ")).trim();
        type = type.substring(0, type.indexOf(" ")).trim();
        Object obj = handlerReturnJsonObj(type);
        return obj != null ? Utils.toJson(obj) : "";
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
        if (outClass == null) {
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
            if (obj != null) {
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
                    handlerReturnJsonWithObj(outClass, innerType, obj);
                }
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
        if (innerClass == null) {
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
            if (value != null) {
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
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                // 泛型在运行时已经被擦除了, 通过下面的方式获取的 getType() 总是 java.lang.Object
                // if (field.getType() == fieldClazz) { setField(field, obj, value); }
                Type type = field.getGenericType();
                if (GENERIC_CLASS_NAME.contains(type.toString())) {
                    setField(field, obj, value);
                } else if (type instanceof ParameterizedType) {
                    Type clazzType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (GENERIC_CLASS_NAME.contains(clazzType.toString()) || clazzType == fieldClazz) {
                        if (clazzType.toString().startsWith(List.class.getName())) {
                            setField(field, obj, Utils.lists(value));
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
            List list = Utils.lists();
            Object object = handlerReturnWithObj(obj);
            if (object != null) {
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
                Map map = Utils.newLinkedHashMap();
                Object key = handlerReturnWithObj(keyValue[0]);
                Object value = handlerReturnWithObj(keyValue[1]);
                map.put(key, value);
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
        if (clazz == null) {
            return null;
        }
        return handlerReturnWithObjClazz(clazz);
    }
    private static Object handlerReturnWithObjClazz(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (Utils.basicType(clazz)) {
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
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                    && field.getAnnotation(ApiReturnIgnore.class) == null) {
                Class<?> type = field.getType();
                // 只在字符串的时候把注解上的值拿来用
                if (type == String.class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String value = "";
                    if (apiReturn != null) {
                        value = apiReturn.desc();
                    }
                    setField(field, obj, value);
                } else if (type == String[].class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String[] value = new String[] { "" };
                    if (apiReturn != null) {
                        value = new String[] { apiReturn.desc() };
                    }
                    setField(field, obj, value);
                }
                else if (type.isEnum()) {
                    // 枚举拿第一个
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
                else if (Utils.basicType(type)) {
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
            return "";
        } else if (clazz == String[].class) {
            return new String[] { "" };
        }

        else {
            return null;
        }
    }
}
