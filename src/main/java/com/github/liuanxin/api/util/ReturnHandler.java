package com.github.liuanxin.api.util;

import com.github.liuanxin.api.annotation.ApiReturn;
import com.github.liuanxin.api.annotation.ApiReturnIgnore;
import com.github.liuanxin.api.model.DocumentReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.*;
import java.util.*;

public final class ReturnHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnHandler.class);
    public static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String LEVEL_APPEND = " -> ";
    private static final Date TMP_DATE = new Date();

    @SuppressWarnings("unchecked")
    private static final List<String> GENERIC_CLASS_NAME = Tools.lists("T", "E", "A", "K", "V");

    public static List<DocumentReturn> handlerReturn(String method, String returnType) {
        List<DocumentReturn> returnList = new LinkedList<>();
        handlerReturn(Tools.EMPTY, Tools.EMPTY, method, returnType, returnList);
        return returnList;
    }

    private static void handlerReturn(String space, String parent, String method,
                                      String type, List<DocumentReturn> returnList) {
        if (Tools.isEmpty(type) || "void".equals(type)) {
            return;
        }
        String responseEntity = ResponseEntity.class.getName();
        if (type.equals(responseEntity) || type.startsWith(responseEntity + "<")) {
            if (type.contains("<") && type.contains(">")) {
                type = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            } else {
                return;
            }
        }

        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if (Tools.isEmpty(className) || "void".equals(className)) {
            return;
        }
        // 「class java.lang.Object」 etc ...
        Class<?> outClass = getClass(className);
        if (Tools.isEmpty(outClass)) {
            return;
        }
        if (outClass.isInterface()) {
            if (Collection.class.isAssignableFrom(outClass)) {
                if (type.contains("<") && type.contains(">")) {
                    String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    handlerReturn(space, parent, method, classType, returnList);
                }
            } else if (Map.class.isAssignableFrom(outClass)) {
                if (type.contains("<") && type.contains(">")) {
                    String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    String[] keyValue = keyAndValue.split(",");
                    if (keyValue.length == 2) {
                        // key must has basic type or enum
                        Class<?> keyClazz;
                        try {
                            keyClazz = Class.forName(keyValue[0].trim());
                        } catch (ClassNotFoundException e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("method ({}) ==> map key({}) has not found", method, keyAndValue);
                            }
                            return;
                        }
                        Object key = Tools.mapKeyDefault(keyClazz);
                        if (Tools.isBlank(key)) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("method ({}) ==> map key({}) type has problem", method, keyAndValue);
                            }
                            return;
                        }

                        DocumentReturn mapKey = new DocumentReturn();
                        mapKey.setName(space + key.toString() + parent).setType(Tools.getInputType(keyClazz));
                        if (keyClazz.isEnum()) {
                            mapKey.setDesc(Tools.descInfo(keyClazz, "enum"));
                        }
                        // add key
                        returnList.add(mapKey);

                        // handle value
                        String innerParent = (LEVEL_APPEND + key.toString() + parent);
                        handlerReturn(space + TAB, innerParent, method, keyValue[1].trim(), returnList);
                    } else {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("method ({}) ==> map returnType({}) has problem", method, keyAndValue);
                        }
                    }
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("method ({}) ==> Unhandled interface class(just Collection or Map)", method);
                }
            }
        } else if (Tools.notBasicType(outClass)) {
            Map<String, String> tmpFieldMap = Tools.newHashMap();
            for (Field field : outClass.getDeclaredFields()) {
                int mod = field.getModifiers();
                // field not static, not final, and not annotation ignore
                if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                        && Tools.isEmpty(field.getAnnotation(ApiReturnIgnore.class))) {
                    String fieldName = field.getName();
                    returnList.add(returnInfo(field, space + fieldName + parent));

                    Class<?> fieldType = field.getType();
                    // not Date Time Timestamp to recursive handle
                    if (!Date.class.isAssignableFrom(fieldType) /* && not other type */ ) {
                        // if not basic type, recursive handle
                        boolean notRecursive = notRecursiveGeneric(outClass, field);
                        if (notRecursive) {
                            String genericType = field.getGenericType().toString();
                            if (Tools.notBasicType(fieldType)) {
                                String innerParent = (LEVEL_APPEND + fieldName + parent);
                                handlerReturn(space + TAB, innerParent, method, genericType, returnList);
                            }
                            tmpFieldMap.put(genericType, fieldName);
                        }
                    }
                }
            }
            // handler generic
            if (type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                String fieldName = handlerReturnFieldName(tmpFieldMap, innerType);
                String innerParent = (LEVEL_APPEND + fieldName + parent);
                handlerReturn(space + TAB, innerParent, method, innerType, returnList);
            }
        }
        /*
        // ignore basic type, if add, comment in example will error
        else {
            String name = outClass.getSimpleName();
            returnList.add(new DocumentReturn(name, name, name));
        }
        */
    }

    /** collect return info */
    private static DocumentReturn returnInfo(Field field, String name) {
        Class<?> fieldType = field.getType();
        DocumentReturn documentReturn = new DocumentReturn();
        documentReturn.setName(name).setType(Tools.getInputType(fieldType));

        ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
        if (Tools.isNotEmpty(apiReturn)) {
            documentReturn.setDesc(apiReturn.value());
            String returnType = apiReturn.type();
            if (Tools.isNotEmpty(returnType)) {
                documentReturn.setType(returnType);
            }
        }
        if (fieldType.isEnum()) {
            documentReturn.setDesc(Tools.descInfo(fieldType, documentReturn.getDesc()));
        }
        return documentReturn;
    }

    private static String handlerReturnFieldName(Map<String, String> fieldMap, String innerType) {
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            for (String className : GENERIC_CLASS_NAME) {
                if (key.equals(className) || key.contains("<" + className  + ">")) {
                    return entry.getValue();
                }
            }
        }

        String innerOutType = innerType;
        if (innerType.contains("<")) {
            innerOutType = innerType.substring(0, innerType.indexOf("<")).trim();
        }
        String name = fieldMap.get(innerOutType);

        if (Tools.isEmpty(name)) {
            name = fieldMap.get(Object.class.getName());
        }
        return Tools.isEmpty(name) ? Tools.EMPTY : name;
    }

    // ========== json ==========

    public static String handlerReturnJson(String method, String returnType) {
        Object obj = handlerReturnJsonObj(method, returnType);
        return Tools.isNotEmpty(obj) ? Tools.toJson(obj) : Tools.EMPTY;
    }

    private static Object handlerReturnJsonObj(String method, String type) {
        if (Tools.isEmpty(type) || "void".equals(type)) {
            return null;
        }
        String responseEntity = ResponseEntity.class.getName();
        if (type.equals(responseEntity) || type.startsWith(responseEntity + "<")) {
            if (type.contains("<") && type.contains(">")) {
                type = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            } else {
                return null;
            }
        }

        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if (Tools.isEmpty(className) || "void".equals(className)) {
            return null;
        }
        // 「class java.lang.Object」 etc ...
        Class<?> outClass = getClass(className);
        if (Tools.isEmpty(outClass)) {
            return null;
        }
        if (outClass.isInterface()) {
            if (Collection.class.isAssignableFrom(outClass)) {
                return handlerReturnJsonList(method, type, outClass);
            } else if (Map.class.isAssignableFrom(outClass)) {
                return handlerReturnJsonMap(method, type);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("method ({}) ==> Unhandled interface class(just Collection or Map)", method);
                }
            }
        } else {
            Object obj = handlerReturnWithObjClazz(method, outClass);
            if (Tools.isNotEmpty(obj) && type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                handlerReturnJsonWithObj(outClass, method, innerType, obj);
            }
            return obj;
        }
        return null;
    }

    private static void handlerReturnJsonWithObj(Class<?> outClass, String method, String type, Object obj) {
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if ("void".equals(className)) {
            return;
        }
        // 「class java.lang.Object」 etc ...
        Class<?> innerClass = getClass(className);
        if (Tools.isEmpty(innerClass)) {
            return;
        }
        if (innerClass.isInterface()) {
            if (Collection.class.isAssignableFrom(innerClass)) {
                setData(outClass, Collection.class, obj, handlerReturnJsonList(method, type, innerClass));
            } else if (Map.class.isAssignableFrom(innerClass)) {
                setData(outClass, Map.class, obj, handlerReturnJsonMap(method, type));
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("method ({}) ==> Unhandled interface class(just Collection or Map)", method);
                }
            }
        } else {
            Object value = handlerReturnWithObjClazz(method, innerClass);
            if (Tools.isNotEmpty(value)) {
                setData(outClass, innerClass, obj, value);
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    handlerReturnJsonWithObj(innerClass, method, innerType, value);
                }
            }
        }
    }


    private static Class<?> getClass(String className) {
        if (className.contains(" ")) {
            className = className.substring(className.indexOf(" ")).trim();
        }
        Class<?> clazz = Tools.getBasicType(className);
        if (Tools.isEmpty(clazz)) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return clazz;
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
            if (Tools.isNotBlank(object)) {
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

                // key must has basic type or enum
                Class<?> keyClazz;
                try {
                    keyClazz = Class.forName(keyValue[0].trim());
                } catch (ClassNotFoundException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("method ({}) ==> handle json, map key({}) has not found", method, keyAndValue);
                    }
                    return Collections.emptyMap();
                }
                Object key = Tools.mapKeyDefault(keyClazz);
                if (Tools.isBlank(key)) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("method ({}) ==> handle json, map key({}) type has problem", method, keyAndValue);
                    }
                    return Collections.emptyMap();
                } else {
                    Object value = handlerReturnJsonObj(method, keyValue[1].trim());
                    // key may be empty String: ""
                    if (Tools.isNotBlank(value)) {
                        map.put(key, value);
                    }
                    return map;
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("method ({}) ==> map returnType({}) has problem", method, keyAndValue);
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Object handlerReturnWithObjClazz(String method, Class<?> clazz) {
        if (Tools.isEmpty(clazz) || clazz == Object.class) {
            return null;
        }
        if (Tools.basicType(clazz)) {
            return Tools.getReturnType(clazz);
        } else if (clazz.isArray()) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("method ({}) ==> The entity({}) on return class is an array, unable to instantiate, " +
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
                LOGGER.warn("The entity({}) on return class can't constructor, " +
                        "please ignore this url. here will be ignored return", clazz.getName());
            }
            return null;
        }
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)
                    && Tools.isEmpty(field.getAnnotation(ApiReturnIgnore.class))) {
                Class<?> type = field.getType();
                // if type is String, use the annotation comment with the value
                if (type == String.class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String value;
                    if (Tools.isNotEmpty(apiReturn)) {
                        value = apiReturn.example();
                        if (Tools.isEmpty(value)) {
                            value = apiReturn.value();
                        }
                    } else {
                        value = Tools.EMPTY;
                    }
                    setField(field, obj, value);
                } else if (type == String[].class) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String[] value;
                    if (Tools.isNotEmpty(apiReturn)) {
                        String example = apiReturn.example();
                        if (Tools.isEmpty(example)) {
                            example = apiReturn.value();
                        }
                        value = new String[] { example };
                    } else {
                        value = new String[] { Tools.EMPTY };
                    }
                    setField(field, obj, value);
                }
                else if (Tools.basicType(type)) {
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    String example;
                    if (Tools.isEmpty(apiReturn)) {
                        example = null;
                    } else {
                        example = apiReturn.example();
                    }
                    setField(field, obj, Tools.getReturnTypeExample(type, example));
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
                            setField(field, obj, handlerReturnWithObjClazz(method, getClass(genericInfo)));
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
            if (Tools.isNotEmpty(signature)) {
                String fieldInfo = signature.toString();
                if (Tools.isNotEmpty(fieldInfo)) {
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
