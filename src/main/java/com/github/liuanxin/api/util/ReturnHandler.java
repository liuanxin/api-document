package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiReturn;
import com.github.liuanxin.api.annotation.ApiReturnIgnore;
import com.github.liuanxin.api.model.DocumentReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

public final class ReturnHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnHandler.class);
    public static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    private static final String SPACE = " ";
    private static final Date TMP_DATE = new Date();

    @SuppressWarnings("unchecked")
    private static final List<String> GENERIC_CLASS_NAME = Tools.lists("T", "E", "A", "K", "V");

    @SuppressWarnings("unchecked")
    public static List<DocumentReturn> handlerReturn(String method, boolean recordLevel) {
        String type = method.substring(method.indexOf(SPACE)).trim();
        type = type.substring(0, type.indexOf(SPACE)).trim();
        List<DocumentReturn> returnList = new ArrayList<>();
        handlerReturn(Tools.EMPTY, Tools.EMPTY, recordLevel, type, returnList);
        return returnList;
    }

    private static void handlerReturn(String space, String parent,
                                      boolean recordLevel, String type,
                                      List<DocumentReturn> returnList) {
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if ("void".equals(className)) {
            return;
        }
        // 「class java.lang.Object」 etc ...
        if (className.contains(" ")) {
            className = className.substring(className.indexOf(" ")).trim();
        }
        Class<?> outClass = null;
        try {
            outClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(outClass)) {
            return;
        }
        if (outClass.isInterface()) {
            if (Collection.class.isAssignableFrom(outClass)) {
                if (type.contains("<") && type.contains(">")) {
                    String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    handlerReturn(space, parent, recordLevel, classType, returnList);
                }
            } else if (Map.class.isAssignableFrom(outClass)) {
                if (type.contains("<") && type.contains(">")) {
                    String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    String[] keyValue = keyAndValue.split(",");
                    if (keyValue.length == 2) {
                        // handlerReturn(space, parent, keyValue[0].trim(), returnList);
                        // just handler value, key don't handler
                        handlerReturn(space, parent, recordLevel, keyValue[1].trim(), returnList);
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("method ({}) map returnType({}) has problem", type, keyAndValue);
                        }
                    }
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unhandled interface class(just Collection or Map): {}", type);
                }
            }
        } else {
            if (Tools.notBasicType(outClass)) {
                Map<String, String> tmpFieldMap = Tools.newHashMap();
                for (Field field : outClass.getDeclaredFields()) {
                    int mod = field.getModifiers();
                    // field not static, not final, and not annotation ignore
                    if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                            && Tools.isBlank(field.getAnnotation(ApiReturnIgnore.class))) {
                        String fieldName = field.getName();
                        returnList.add(returnInfo(field, space + fieldName + parent));

                        // if not basic type, recursive handle
                        boolean notRecursive = notRecursiveGeneric(outClass, field);
                        if (notRecursive) {
                            String genericType = field.getGenericType().toString();
                            if (Tools.notBasicType(field.getType())) {
                                String innerParent = recordLevel ? (" -> " + fieldName + parent) : Tools.EMPTY;
                                handlerReturn(space + TAB, innerParent, recordLevel, genericType, returnList);
                            }
                            tmpFieldMap.put(genericType, fieldName);
                        }
                    }
                }
                // handler generic
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    String fieldName = handlerReturnFieldName(tmpFieldMap, innerType, recordLevel);
                    String innerParent = recordLevel ? (" -> " + fieldName + parent) : Tools.EMPTY;
                    handlerReturn(space + TAB, innerParent, recordLevel, innerType, returnList);
                }
            }
        }
    }

    /** collect return info */
    private static DocumentReturn returnInfo(Field field, String name) {
        Class<?> fieldType = field.getType();
        DocumentReturn documentReturn = new DocumentReturn();
        documentReturn.setName(name).setType(Tools.getInputType(fieldType));

        ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
        if (Tools.isNotBlank(apiReturn)) {
            documentReturn.setDesc(apiReturn.value());
            String returnType = apiReturn.type();
            if (Tools.isNotBlank(returnType)) {
                documentReturn.setType(returnType);
            }
        }
        if (fieldType.isEnum()) {
            // enum append (code:value)
            String desc = documentReturn.getDesc();
            String enumInfo = Tools.enumInfo(fieldType);
            documentReturn.setDesc(Tools.isBlank(desc) ? enumInfo : (desc + "(" + enumInfo + ")"));
        }
        return documentReturn;
    }

    private static String handlerReturnFieldName(Map<String, String> fieldMap, String innerType, boolean recordLevel) {
        if (!recordLevel) {
            return Tools.EMPTY;
        }
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            for (String className : GENERIC_CLASS_NAME) {
                if (key.equals(className)) {
                    return entry.getValue();
                }
            }
        }

        String innerOutType = innerType;
        if (innerType.contains("<")) {
            innerOutType = innerType.substring(0, innerType.indexOf("<")).trim();
        }
        String name = fieldMap.get(innerOutType);

        if (Tools.isBlank(name)) {
            name = fieldMap.get(Object.class.getName());
        }
        return Tools.isBlank(name) ? Tools.EMPTY : name;
    }

    // ========== json ==========

    public static String handlerReturnJson(String method) {
        String type = method.substring(method.indexOf(SPACE)).trim();
        type = type.substring(0, type.indexOf(SPACE)).trim();
        Object obj = handlerReturnJsonObj(method, type);
        return Tools.isNotBlank(obj) ? Tools.toJson(obj) : Tools.EMPTY;
    }

    private static Object handlerReturnJsonObj(String method, String type) {
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if ("void".equals(className)) {
            return null;
        }
        // 「class java.lang.Object」 etc ...
        if (className.contains(" ")) {
            className = className.substring(className.indexOf(" ")).trim();
        }
        Class<?> outClass = null;
        try {
            outClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(outClass)) {
            return null;
        }
        if (outClass.isInterface()) {
            if (Collection.class.isAssignableFrom(outClass)) {
                return handlerReturnJsonList(method, type, outClass);
            } else if (Map.class.isAssignableFrom(outClass)) {
                return handlerReturnJsonMap(method, type);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unhandled interface class(just Collection or Map): {}", type);
                }
            }
        } else {
            Object obj = handlerReturnWithObjClazz(method, outClass);
            if (Tools.isNotBlank(obj) && type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                handlerReturnJsonWithObj(method, outClass, innerType, obj);
            }
            return obj;
        }
        return null;
    }

    private static void handlerReturnJsonWithObj(String method, Class<?> outClass, String type, Object obj) {
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if ("void".equals(className)) {
            return;
        }
        // 「class java.lang.Object」 etc ...
        if (className.contains(" ")) {
            className = className.substring(className.indexOf(" ")).trim();
        }
        Class<?> innerClass = null;
        try {
            innerClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(innerClass)) {
            return;
        }
        if (innerClass.isInterface()) {
            if (Collection.class.isAssignableFrom(innerClass)) {
                setData(outClass, Collection.class, obj, handlerReturnJsonList(method, type, innerClass));
            } else if (Map.class.isAssignableFrom(innerClass)) {
                setData(outClass, Map.class, obj, handlerReturnJsonMap(method, type));
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unhandled interface class(just Collection or Map): {}", type);
                }
            }
        } else {
            Object value = handlerReturnWithObjClazz(method, innerClass);
            if (Tools.isNotBlank(value)) {
                setData(outClass, innerClass, obj, value);
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    handlerReturnJsonWithObj(method, innerClass, innerType, value);
                }
            }
        }
    }

    private static void setData(Class<?> clazz, Class<?> fieldClazz, Object obj, Object value) {
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                // Generics have been erased at runtime, Following way getType() is always java.lang.Object
                // if (field.getType() == fieldClazz) { setField(field, obj, value); }
                Type type = field.getGenericType();
                if (GENERIC_CLASS_NAME.contains(type.toString())) {
                    setField(field, obj, value);
                } else if (type instanceof ParameterizedType) {
                    Type clazzType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (GENERIC_CLASS_NAME.contains(clazzType.toString()) || clazzType == fieldClazz) {
                        Class<?> tmpClazz = getParameterizedType(type);
                        Class<?> tmpType = getParameterizedType(clazzType);
                        if ((tmpClazz != null && Collection.class.isAssignableFrom(tmpClazz))
                                || (tmpType != null && Collection.class.isAssignableFrom(tmpType))) {
                            setField(field, obj, Tools.lists(value));
                        } else {
                            setField(field, obj, value);
                        }
                    }
                }
            }
        }
    }

    private static Class<?> getParameterizedType(Type type) {
        Class<?> tmpType = null;
        try {
            String className = type.toString();
            if (className.contains("<") && className.contains(">")) {
                className = className.substring(0, className.indexOf("<")).trim();
            }
            // 「class java.lang.Object」 etc ...
            if (className.contains(" ")) {
                className = className.substring(className.indexOf(" ")).trim();
            }
            tmpType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return tmpType;
    }

    @SuppressWarnings("unchecked")
    private static Collection handlerReturnJsonList(String method, String type, Class clazz) {
        if (type.contains("<") && type.contains(">")) {
            String obj = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            // add one record in list
            Collection list;
            if (Set.class.isAssignableFrom(clazz)) {
                list = new HashSet();
            } else {
                list = new ArrayList();
            }
            Object object = handlerReturnJsonObj(method, obj);
            if (!Tools.isBlankObj(object)) {
                list.add(object);
            }
            return list;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map handlerReturnJsonMap(String method, String type) {
        if (type.contains("<") && type.contains(">")) {
            // add one key:value in map
            String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
            String[] keyValue = keyAndValue.split(",");
            if (keyValue.length == 2) {
                Map map = Tools.newHashMap();
                // key can't be List or Map
                Object key = handlerReturnWithObj(method, keyValue[0].trim());
                Object value = handlerReturnJsonObj(method, keyValue[1].trim());
                // key may be empty String: ""
                if (!Tools.isBlankObj(key) && !Tools.isBlankObj(value)) {
                    map.put(key, value);
                }
                return map;
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("method ({}) map returnType({}) has problem", type, keyAndValue);
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Object handlerReturnWithObj(String method, String className) {
        // 「class java.lang.Object」 etc ...
        if (className.contains(" ")) {
            className = className.substring(className.indexOf(" ")).trim();
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (Tools.isBlank(clazz)) {
            return null;
        }
        return handlerReturnWithObjClazz(method, clazz);
    }

    private static Object handlerReturnWithObjClazz(String method, Class<?> clazz) {
        if (Tools.isBlank(clazz) || clazz == Object.class) {
            return null;
        }
        if (Tools.basicType(clazz)) {
            return Tools.getReturnType(clazz);
        } else if (clazz.isArray()) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("In the ({}) method, The entity({}) on return class is an array, unable to instantiate, " +
                        "please use List to replace. here will be ignored return", method, clazz.getName());
            }
            return null;
        }

        Object obj;
        try {
            obj = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            // return type must have constructor with default
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("In the ({}) method, The entity({}) on return class can't constructor, " +
                        "please ignore this url. here will be ignored return", method, clazz.getName());
            }
            return null;
        }
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                    && Tools.isBlank(field.getAnnotation(ApiReturnIgnore.class))) {
                Class<?> type = field.getType();
                // if type is String, use the annotation comment with the value
                if (type == String.class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String value = Tools.EMPTY;
                    if (Tools.isNotBlank(apiReturn)) {
                        value = apiReturn.value();
                    }
                    setField(field, obj, value);
                } else if (type == String[].class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String[] value = new String[] { Tools.EMPTY };
                    if (Tools.isNotBlank(apiReturn)) {
                        value = new String[] { apiReturn.value() };
                    }
                    setField(field, obj, value);
                }
                else if (Tools.basicType(type)) {
                    setField(field, obj, Tools.getReturnType(type));
                }
                else if (Date.class.isAssignableFrom(type)) {
                    setField(field, obj, TMP_DATE);
                }
                else {
                    boolean notRecursive = notRecursiveGeneric(clazz, field);
                    if (notRecursive) {
                        String genericInfo = field.getGenericType().toString();
                        if (Collection.class.isAssignableFrom(type)) {
                            setField(field, obj, handlerReturnJsonList(method, genericInfo, type));
                        } else if (Map.class.isAssignableFrom(type)) {
                            setField(field, obj, handlerReturnJsonMap(method, genericInfo));
                        } else {
                            setField(field, obj, handlerReturnWithObj(method, genericInfo));
                        }
                    }
                }
            }
        }
        return obj;
    }

    private static void setField(Field field, Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format("Cannot assignment field %s to %s with %s", field, value, obj), e);
            }
        }
    }

    /** if not recursive class(level > 2 will error T_T ), return true */
    private static boolean notRecursiveGeneric(Class clazz, Field field) {
        try {
            Field signatureField = field.getClass().getDeclaredField("signature");
            signatureField.setAccessible(true);
            Object signature = signatureField.get(field);
            if (Tools.isNotBlank(signature)) {
                String fieldInfo = signature.toString();
                if (Tools.isNotBlank(fieldInfo)) {
                    if (fieldInfo.contains("/") && fieldInfo.contains("<") && fieldInfo.contains(">")) {
                        return !fieldInfo.replace("/", ".").contains(clazz.getName() + ";>;");
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format("class(%s), field(%s)", clazz, field.getName()), e);
            }
        }
        return true;
    }
}
