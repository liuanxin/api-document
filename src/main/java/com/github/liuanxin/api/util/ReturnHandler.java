package com.github.liuanxin.api.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.liuanxin.api.annotation.ApiModel;
import com.github.liuanxin.api.annotation.ApiReturn;
import com.github.liuanxin.api.annotation.ApiReturnIgnore;
import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.model.DocumentReturn;
import com.github.liuanxin.api.model.Recursive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings("DuplicatedCode")
public final class ReturnHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnHandler.class);
    public static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String LEVEL_APPEND = " -> ";

    private static final String CALLABLE = Callable.class.getName();
    private static final String RESPONSE_ENTITY = ResponseEntity .class.getName();
    private static final String DEFERRED_RESULT = DeferredResult.class.getName();

    private static final List<String> GENERIC_CLASS_NAME = new ArrayList<>(Arrays.asList("T", "E", "A", "K", "V"));

    public static List<DocumentReturn> handlerReturn(String method, String returnType) {
        List<DocumentReturn> returnList = new LinkedList<>();
        handlerReturn(null, null, ApiConst.EMPTY, ApiConst.EMPTY, method, returnType, returnList);
        return returnList;
    }

    private static void handlerReturn(Recursive parentRecursive, String fieldName,
                                      String space, String parent, String method,
                                      String type, List<DocumentReturn> returnList) {
        if (Tools.isEmpty(type) || "void".equals(type)) {
            return;
        }
        if (type.equals(CALLABLE) || type.startsWith(CALLABLE + "<")
                || type.equals(RESPONSE_ENTITY) || type.startsWith(RESPONSE_ENTITY + "<")
                || type.equals(DEFERRED_RESULT) || type.startsWith(DEFERRED_RESULT + "<")) {
            if (type.contains("<") && type.contains(">")) {
                type = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            } else {
                return;
            }
        }

        // com.xxx.JsonResult<com.xxx.vo.XyzVo>> => com.xxx.JsonResult
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if (Tools.isEmpty(className) || "void".equals(className)) {
            return;
        }
        Class<?> outClass = getClass(className);
        if (Tools.isEmpty(outClass)) {
            return;
        }

        if (Collection.class.isAssignableFrom(outClass)) {
            if (type.contains("<") && type.contains(">")) {
                String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                handlerReturn(parentRecursive, fieldName, space, parent, method, classType, returnList);
            }
        }
        else if (Map.class.isAssignableFrom(outClass)) {
            if (type.contains("<") && type.contains(">")) {
                String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                String[] keyValue = keyAndValue.split(",");
                if (keyValue.length == 2) {
                    // key must has basic type or enum
                    Class<?> keyClazz;
                    try {
                        keyClazz = Class.forName(keyValue[0].trim());
                    } catch (ClassNotFoundException ignore) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("method {} ==> map key({}) has not found", method, keyAndValue);
                        }
                        return;
                    }
                    Object key = Tools.mapKeyDefault(keyClazz);
                    if (Tools.isBlank(key)) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("method {} ==> map key({}) just handle basic type", method, keyAndValue);
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
                    handlerReturn(parentRecursive, fieldName, space + TAB, innerParent, method, keyValue[1].trim(), returnList);
                }
            }
        }
        else if (Tools.notBasicType(outClass)) {
            Recursive selfRecursive = new Recursive(parentRecursive, fieldName, outClass);
            Map<String, String> tmpFieldMap = Tools.newHashMap();
            for (Field field : outClass.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                    JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
                    boolean ignore = Tools.isEmpty(jsonIgnore) || !jsonIgnore.value();
                    if (ignore && Tools.isEmpty(field.getAnnotation(ApiReturnIgnore.class))) {
                        String name = null;
                        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                        if (Tools.isNotBlank(jsonProperty)) {
                            name = jsonProperty.value();
                        } else {
                            ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                            if (Tools.isNotBlank(apiReturn)) {
                                name = apiReturn.name();
                            } else {
                                ApiModel apiModel = field.getAnnotation(ApiModel.class);
                                if (Tools.isNotBlank(apiModel)) {
                                    name = apiModel.name();
                                }
                            }
                        }
                        if (Tools.isEmpty(name)) {
                            name = field.getName();
                        }
                        returnList.add(returnInfo(field, space + name + parent));

                        Class<?> fieldType = getArrayType(field.getType());
                        // not Date Time Timestamp to recursive handle
                        if (!Date.class.isAssignableFrom(fieldType) /* && not other type */) {
                            String genericType = field.getGenericType().toString();
                            if (Tools.notBasicType(fieldType)) {
                                Recursive childRecursive = new Recursive(selfRecursive, name, genericType);
                                if (childRecursive.checkRecursive()) {
                                    if (LOGGER.isWarnEnabled()) {
                                        LOGGER.warn("!!!method {} field({}) ==> return type recursive!!!",
                                                method, childRecursive.getOrbit());
                                    }
                                    DocumentReturn last = returnList.get(returnList.size() - 1);
                                    String desc = last.getDesc();
                                    last.setDesc("!!!RECURSIVE OBJECT!!!" + (Tools.isEmpty(desc) ? ApiConst.EMPTY : (ApiConst.SPACE + desc)));
                                } else {
                                    String innerParent = (LEVEL_APPEND + name + parent);
                                    handlerReturn(selfRecursive, name, space + TAB, innerParent, method, genericType, returnList);
                                }
                            }
                            tmpFieldMap.put(genericType, name);
                        }
                    }
                }
            }
            // handler generic
            if (type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                String name = handlerReturnFieldName(tmpFieldMap, innerType);
                String innerParent = (LEVEL_APPEND + name + parent);
                handlerReturn(selfRecursive, name, space + TAB, innerParent, method, innerType, returnList);
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

    private static Class<?> getArrayType(Class<?> clazz) {
        if (clazz.isArray()) {
            return getArrayType(clazz.getComponentType());
        } else {
            return clazz;
        }
    }

    /** collect return info */
    private static DocumentReturn returnInfo(Field field, String name) {
        Class<?> fieldType = field.getType();
        if (Collection.class.isAssignableFrom(fieldType)) {
            String type = field.getGenericType().toString();
            String classType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            try {
                fieldType = Class.forName(classType);
            } catch (ClassNotFoundException ignore) {
            }
        }
        DocumentReturn documentReturn = new DocumentReturn();
        documentReturn.setName(name).setType(Tools.getInputType(fieldType));

        String desc = null;
        ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
        if (Tools.isNotBlank(apiReturn)) {
            desc = apiReturn.value();
            String returnType = apiReturn.type();
            if (Tools.isNotEmpty(returnType)) {
                documentReturn.setType(returnType);
            }
        } else {
            ApiModel apiModel = field.getAnnotation(ApiModel.class);
            if (Tools.isNotBlank(apiModel)) {
                desc = apiModel.value();
                String returnType = apiModel.dataType();
                if (Tools.isNotEmpty(returnType)) {
                    documentReturn.setType(returnType);
                }
            }
        }
        if (Tools.isBlank(desc)) {
            desc = ApiConst.EMPTY;
        }
        documentReturn.setDesc(Tools.descInfo(fieldType, desc));
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
        return Tools.isEmpty(name) ? ApiConst.EMPTY : name;
    }

    // ========== json ==========

    public static String handlerReturnJson(String method, String returnType) {
        Object obj = handlerReturnJsonObj(null, null, method, returnType);
        return Tools.isNotEmpty(obj) ? Tools.toJson(obj) : ApiConst.EMPTY;
    }

    private static Object handlerReturnJsonObj(Recursive parentRecursive, String fieldName, String method, String type) {
        if (Tools.isEmpty(type) || "void".equals(type)) {
            return null;
        }
        if (type.equals(CALLABLE) || type.startsWith(CALLABLE + "<")
                || type.equals(RESPONSE_ENTITY) || type.startsWith(RESPONSE_ENTITY + "<")
                || type.equals(DEFERRED_RESULT) || type.startsWith(DEFERRED_RESULT + "<")) {
            if (type.contains("<") && type.contains(">")) {
                type = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            } else {
                return null;
            }
        }

        // com.xxx.JsonResult<com.xxx.vo.XyzVo>> => com.xxx.JsonResult
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if (Tools.isEmpty(className) || "void".equals(className)) {
            return null;
        }
        Class<?> outClass = getClass(className);
        if (Tools.isBlank(outClass)) {
            return null;
        }

        if (Collection.class.isAssignableFrom(outClass)) {
            return handlerReturnJsonList(parentRecursive, fieldName, method, type, outClass);
        }
        else if (Map.class.isAssignableFrom(outClass)) {
            return handlerReturnJsonMap(parentRecursive, fieldName, method, type);
        }
        else {
            Object obj = handlerReturnWithObjClazz(parentRecursive, fieldName, method, outClass);
            if (Tools.isNotEmpty(obj) && type.contains("<") && type.contains(">")) {
                String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                handlerReturnJsonWithObj(parentRecursive, fieldName, outClass, method, innerType, obj);
            }
            return obj;
        }
    }

    private static void handlerReturnJsonWithObj(Recursive parentRecursive, String fieldName, Class<?> outClass,
                                                 String method, String type, Object obj) {
        String className = type.contains("<") ? type.substring(0, type.indexOf("<")).trim() : type;
        if (Tools.isEmpty(className) || "void".equals(className)) {
            return;
        }
        Class<?> innerClass = getClass(className);
        if (Tools.isBlank(innerClass)) {
            return;
        }
        if (Collection.class.isAssignableFrom(innerClass)) {
            setData(outClass, innerClass, obj, handlerReturnJsonList(parentRecursive, fieldName, method, type, innerClass));
        }
        else if (Map.class.isAssignableFrom(innerClass)) {
            setData(outClass, innerClass, obj, handlerReturnJsonMap(parentRecursive, fieldName, method, type));
        }
        else {
            Object value = handlerReturnWithObjClazz(parentRecursive, fieldName, method, innerClass);
            if (Tools.isNotEmpty(value)) {
                setData(outClass, innerClass, obj, value);
                if (type.contains("<") && type.contains(">")) {
                    String innerType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
                    handlerReturnJsonWithObj(parentRecursive, fieldName, innerClass, method, innerType, value);
                }
            }
        }
    }


    private static Class<?> getClass(String className) {
        if (className.contains(ApiConst.SPACE)) {
            className = className.substring(className.indexOf(ApiConst.SPACE)).trim();
        }
        Class<?> clazz = Tools.getBasicType(className);
        if (Tools.isEmpty(clazz)) {
            try {
                clazz = Class.forName(className);
                if (clazz == Void.class) {
                    return null;
                }
            } catch (ClassNotFoundException ignore) {
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
                        if ((Tools.isNotBlank(tmpClazz) && Collection.class.isAssignableFrom(tmpClazz))
                                || (Tools.isNotBlank(tmpType) && Collection.class.isAssignableFrom(tmpType))) {
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
            if (className.contains(ApiConst.SPACE)) {
                className = className.substring(className.indexOf(ApiConst.SPACE)).trim();
            }
            tmpType = Class.forName(className);
            if (tmpType == Void.class) {
                return null;
            }
        } catch (ClassNotFoundException ignore) {
        }
        return tmpType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Collection handlerReturnJsonList(Recursive parentRecursive, String fieldName,
                                                    String method, String type, Class<?> clazz) {
        if (type.contains("<") && type.contains(">")) {
            String obj = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
            // add one record in list
            Collection list;
            if (LinkedHashSet.class.isAssignableFrom(clazz)) {
                list = new LinkedHashSet();
            } else if (Set.class.isAssignableFrom(clazz)) {
                list = new HashSet();
            } else if (LinkedList.class.isAssignableFrom(clazz)) {
                list = new LinkedList();
            } else {
                list = new ArrayList();
            }
            Object object = handlerReturnJsonObj(parentRecursive, fieldName, method, obj);
            if (Tools.isNotBlank(object)) {
                list.add(object);
            }
            return list;
        } else {
            return Set.class.isAssignableFrom(clazz) ? Collections.emptySet() : Collections.emptyList();
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map handlerReturnJsonMap(Recursive parentRecursive, String fieldName, String method, String type) {
        if (type.contains("<") && type.contains(">")) {
            // add one key:value in map
            String keyAndValue = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
            String[] keyValue = keyAndValue.split(",");
            if (keyValue.length == 2) {
                // key must has basic type or enum
                Class<?> keyClazz;
                try {
                    keyClazz = Class.forName(keyValue[0].trim());
                } catch (ClassNotFoundException ignore) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("method {} ==> handle json, map key({}) has not found", method, keyAndValue);
                    }
                    return Collections.emptyMap();
                }
                Object key = Tools.mapKeyDefault(keyClazz);
                if (Tools.isBlank(key)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("method {} ==> handle json, map key({}) just handle basic type", method, keyAndValue);
                    }
                    return Collections.emptyMap();
                } else {
                    // key may be empty String: "", value may by null
                    return Tools.maps(key, handlerReturnJsonObj(parentRecursive, fieldName, method, keyValue[1].trim()));
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Object handlerReturnWithObjClazz(Recursive parentRecursive, String fieldName, String method, Class<?> clazz) {
        if (Tools.isEmpty(clazz) || clazz == Object.class) {
            return null;
        }

        if (Tools.basicType(clazz)) {
            return Tools.getReturnType(clazz);
        }
        else if (clazz.isArray()) {
            Class<?> arrType = clazz.getComponentType();
            Object arr = Array.newInstance(arrType, 1);
            Array.set(arr, 0, handlerReturnWithObjClazz(parentRecursive, fieldName, method, arrType));
            return arr;
        }

        Object obj;
        try {
            obj = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // return type must have constructor with default
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("method {} ==> The entity({}) on return class can't constructor", method, clazz.getName());
            }
            return null;
        }

        Recursive selfRecursive = new Recursive(parentRecursive, fieldName, clazz);
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
                boolean ignore = Tools.isEmpty(jsonIgnore) || !jsonIgnore.value();
                if (ignore && Tools.isEmpty(field.getAnnotation(ApiReturnIgnore.class))) {
                    String name = field.getName();
                    Class<?> fieldType = field.getType();

                    String basicTypeExample = null, example = null;
                    ApiReturn apiReturn = field.getAnnotation(ApiReturn.class);
                    if (Tools.isNotBlank(apiReturn)) {
                        example = apiReturn.example();
                        basicTypeExample = Tools.isEmpty(example) ? apiReturn.value() : example;
                    } else {
                        ApiModel apiModel = field.getAnnotation(ApiModel.class);
                        if (Tools.isNotBlank(apiModel)) {
                            example = apiModel.example();
                            basicTypeExample = Tools.isEmpty(example) ? apiModel.value() : example;
                        }
                    }
                    if (Tools.basicType(fieldType)) {
                        setField(field, obj, Tools.getReturnTypeExample(fieldType, basicTypeExample));
                    } else if (fieldType.isArray()) {
                        Class<?> arrType = fieldType.getComponentType();
                        Object arr = Array.newInstance(arrType, 1);
                        Object object;
                        if (Tools.basicType(arrType)) {
                            object = Tools.getReturnTypeExample(arrType, basicTypeExample);
                        } else {
                            object = handlerReturnWithObjClazz(selfRecursive, name, method, arrType);
                        }
                        Array.set(arr, 0, object);
                        setField(field, obj, arr);
                    } else {
                        String genericInfo = field.getGenericType().toString();
                        Recursive childRecursive = new Recursive(selfRecursive, name, genericInfo);
                        if (!childRecursive.checkRecursive()) {
                            if (Collection.class.isAssignableFrom(fieldType)) {
                                if (genericInfo.contains("<") && genericInfo.contains(">")) {
                                    String objClass = genericInfo.substring(genericInfo.indexOf("<") + 1, genericInfo.lastIndexOf(">")).trim();
                                    Class<?> fieldClass = getClass(objClass);
                                    if (Tools.basicType(fieldClass)) {
                                        setField(field, obj, Collections.singletonList(Tools.getReturnTypeExample(fieldClass, basicTypeExample)));
                                    } else {
                                        setField(field, obj, handlerReturnJsonList(selfRecursive, fieldName, method, genericInfo, fieldType));
                                    }
                                } else {
                                    setField(field, obj, handlerReturnJsonList(selfRecursive, fieldName, method, genericInfo, fieldType));
                                }
                            } else if (Map.class.isAssignableFrom(fieldType)) {
                                setField(field, obj, handlerReturnJsonMap(selfRecursive, fieldName, method, genericInfo));
                            } else {
                                setExample(method, obj, selfRecursive, field, name, fieldType, example, genericInfo);
                            }
                        } /* else {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("!!!method {} field({}) ==> handle json, return type recursive!!!",
                                        method, childRecursive.getOrbit());
                            }
                        } */
                    }
                }
            }
        }
        return obj;
    }

    private static void setExample(String method, Object obj, Recursive selfRecursive, Field field,
                                   String name, Class<?> fieldType, String example, String genericInfo) {
        if (Tools.isNotEmpty(example)) {
            try {
                setField(field, obj, fieldType.getConstructor(String.class).newInstance(example));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Cannot constructor {}, Please add Constructor or remove Example", e.getMessage());
                }
            }
        } else {
            setField(field, obj, handlerReturnWithObjClazz(selfRecursive, name, method, getClass(genericInfo)));
        }
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
}
