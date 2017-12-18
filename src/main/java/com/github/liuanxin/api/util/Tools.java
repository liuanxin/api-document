package com.github.liuanxin.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    public static final String EMPTY = "";
    private static final String SPLIT = ",";

    /** 整数类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> INT_TYPE = lists(
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class
    );
    /** 浮点数类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> DOUBLE_TYPE = lists(
            float.class, Float.class,
            double.class, Double.class,
            BigDecimal.class
    );

    /** 整数的数组类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> INT_ARRAY_TYPE = lists(
            byte[].class, Byte[].class,
            char[].class, Character[].class,
            short[].class, Short[].class,
            int[].class, Integer[].class,
            long[].class, Long[].class
    );
    /** 浮点数的数组类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> DOUBLE_ARRAY_TYPE = lists(
            float[].class, Float[].class,
            double[].class, Double[].class,
            BigDecimal[].class
    );

    /** 基础类型 */
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> BASIC_TYPE = lists(
            String.class, String[].class,
            boolean.class, Boolean.class, boolean[].class, Boolean[].class
    );
    static {
        BASIC_TYPE.addAll(INT_TYPE);
        BASIC_TYPE.addAll(DOUBLE_TYPE);
        BASIC_TYPE.addAll(INT_ARRAY_TYPE);
        BASIC_TYPE.addAll(DOUBLE_ARRAY_TYPE);
    }

    // ========== string ==========
    public static boolean isBlank(Object obj) {
        return obj == null || EMPTY.equals(obj.toString().trim());
    }
    public static boolean isNotBlank(Object obj) {
        return !isBlank(obj);
    }

    // ========== json ==========
    private static final ObjectMapper RENDER = new ObjectMapper();
    public static String toJson(Object obj) {
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
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
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
    public static <T> Set<T> sets() {
        return new HashSet<>();
    }
    public static <T> List<T> lists(Collection<T> values) {
        return new ArrayList<>(values);
    }
    public static <K, V> HashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
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
                    // 收集 enum 的 code 和 value, 都不为空时生成一个字符串返回
                    Object code = getMethod(em, "getCode");
                    Object value = getMethod(em, "getValue");
                    if (isNotBlank(code) && isNotBlank(value)) {
                        sbd.append(code).append(":").append(value).append(", ");
                    } else {
                        sbd.append(em.name()).append(", ");
                    }
                }
                if (sbd.length() > 2) {
                    return sbd.delete(sbd.length() - 2, sbd.length()).toString();
                }
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
    static String getInputType(Class<?> type) {
        if (type == null) {
            return EMPTY;
        }
        String paramType = type.getSimpleName();
        if (Tools.isBlank(paramType)) {
            return EMPTY;
        }
        if (type == String.class || type == String[].class) {
            return paramType.substring(0, 1).toLowerCase() + paramType.substring(1);
        }
        if (type.isEnum()) {
            return "Enum(" + paramType + ")";
        }

        for (Class<?> typeClass : INT_TYPE) {
            if (type == typeClass) {
                return int.class.getSimpleName();
            }
        }
        for (Class<?> typeClass : DOUBLE_TYPE) {
            if (type == typeClass) {
                return double.class.getSimpleName();
            }
        }

        // 逗号分隔(comma-separated)
        for (Class<?> typeClass : INT_ARRAY_TYPE) {
            if (type == typeClass) {
                return int[].class.getSimpleName();
            }
        }
        for (Class<?> typeClass : DOUBLE_ARRAY_TYPE) {
            if (type == typeClass) {
                return double[].class.getSimpleName();
            }
        }
        return paramType.substring(0, 1).toLowerCase() + paramType.substring(1);
    }
}
