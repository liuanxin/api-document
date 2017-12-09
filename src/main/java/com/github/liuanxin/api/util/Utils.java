package com.github.liuanxin.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String EMPTY = "";
    private static final String SPLIT = ",";

    /** 基础类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> BASIC_TYPE = lists(
            String.class, String[].class,
            boolean.class, Boolean.class, boolean[].class, Boolean[].class,
            byte.class, Byte.class, byte[].class, Byte[].class,
            char.class, Character.class, char[].class, Character[].class,
            short.class, Short.class, short[].class, Short[].class,
            int.class, Integer.class, int[].class, Integer[].class,
            long.class, Long.class, long[].class, Long[].class,
            float.class, Float.class, float[].class, Float[].class,
            double.class, Double.class, double[].class, Double[].class
    );

    // ========== json ==========
    private static final ObjectMapper RENDER = new ObjectMapper();
    static String toJson(Object obj) {
        try {
            return RENDER.writeValueAsString(obj);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("to json exception", e);
            }
            return EMPTY;
        }
    }

    // ========== array map ==========
    private static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
    private static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }
    private static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() == 0;
    }
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
    public static <T> String toStr(Collection<T> collection) {
        if (isEmpty(collection)) {
            return EMPTY;
        }
        StringBuilder sbd = new StringBuilder();
        int i = 0;
        for (T t : collection) {
            sbd.append(t);
            if (i + 1 != collection.size()) {
                sbd.append(SPLIT);
            }
            i++;
        }
        return sbd.toString();
    }

    // ========== list map ==========
    @SuppressWarnings("unchecked")
    public static List lists(Object... values) {
        return new ArrayList(Arrays.asList(values));
    }
    public static <T> List<T> lists(Collection<T> values) {
        return new ArrayList<T>(values);
    }
    public static <K, V> HashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    // ========== string ==========
    private static boolean isBlank(Object obj) {
        return obj == null || "".equals(obj.toString().trim());
    }
    /** 对象非空时返回 true */
    private static boolean isNotBlank(Object obj) {
        return !isBlank(obj);
    }

    // ========== method ==========
    private static Object getMethod(Object obj, String method, Object... param) {
        if (isNotBlank(method)) {
            try {
                return obj.getClass().getDeclaredMethod(method).invoke(obj, param);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignore
            }
            // getMethod 会将从父类继承过来的 public 方法也查询出来
            try {
                return obj.getClass().getMethod(method).invoke(obj, param);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                // ignore
            }
        }
        return null;
    }

    // ========== enum ==========
    static String enumInfo(Class<?> clazz) {
        if (isNotBlank(clazz) && clazz.isEnum()) {
            Enum[] constants = (Enum[]) clazz.getEnumConstants();
            if (isNotEmpty(constants)) {
                StringBuilder sbd = new StringBuilder();
                for (Enum em : constants) {
                    Object code = getMethod(em, "getCode");
                    Object value = getMethod(em, "getValue");
                    if (isNotBlank(code)) {
                        sbd.append(code).append(":");
                        if (isNotBlank(value)) {
                            sbd.append(value).append(", ");
                        }
                    } else {
                        sbd.append(em.name()).append(", ");
                    }
                }
                return sbd.delete(sbd.length() - 2, sbd.length()).toString();
            }
        }
        return EMPTY;
    }

    // ========== type ==========
    static boolean basicType(Class<?> clazz) {
        for (Class<?> typeClass : BASIC_TYPE) {
            if (clazz == typeClass) {
                return true;
            }
        }
        // 枚举也当成是基础类型
        return clazz.isEnum();
    }
    static boolean notBasicType(Class<?> clazz) {
        return !basicType(clazz);
    }

    static String getInputType(String paramType) {
        if (paramType == null || "".equals(paramType)) {
            return "";
        }
        else if ("Integer".equals(paramType) || "long".equals(paramType) || "Long".equals(paramType)) {
            return "int";
        }
        else if ("float".equals(paramType) || "Float".equals(paramType)
                || "Double".equals(paramType) || "BigDecimal".equals(paramType)) {
            return "double";
        }
        else {
            return paramType.toLowerCase();
        }
    }
}
