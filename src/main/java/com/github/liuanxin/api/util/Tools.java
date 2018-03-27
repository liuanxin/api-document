package com.github.liuanxin.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    public static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String SPLIT = ",";

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> INT_TYPE = lists(
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class
    );
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> DOUBLE_TYPE = lists(
            float.class, Float.class,
            double.class, Double.class,
            BigDecimal.class
    );

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> INT_ARRAY_TYPE = lists(
            byte[].class, Byte[].class,
            char[].class, Character[].class,
            short[].class, Short[].class,
            int[].class, Integer[].class,
            long[].class, Long[].class
    );
    @SuppressWarnings("unchecked")
    private static final List<Class<?>> DOUBLE_ARRAY_TYPE = lists(
            float[].class, Float[].class,
            double[].class, Double[].class,
            BigDecimal[].class
    );

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
    public static String addPrefix(String src) {
        if (isBlank(src)) {
            return "/";
        }
        if (src.startsWith("/")) {
            return src;
        }
        return "/" + src;
    }

    // ========== json ==========
    private static final ObjectMapper RENDER = new ObjectMapper();
    private static final ObjectWriter PRETTY_RENDER = new ObjectMapper().writerWithDefaultPrettyPrinter();
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
    public static String toPrettyJson(String json) {
        if (isBlank(json)) {
            return EMPTY;
        }
        try {
            return PRETTY_RENDER.writeValueAsString(RENDER.readValue(json, Object.class));
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("to pretty json exception", e);
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
    static List lists(Object... values) {
        return new ArrayList(Arrays.asList(values));
    }
    public static <T> Set<T> sets() {
        return new HashSet<>();
    }
    public static <K, V> HashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    // ========== method ==========
    private static Object getMethod(Object obj, String method, Object... param) {
        if (isNotBlank(method)) {
            try {
                return obj.getClass().getDeclaredMethod(method).invoke(obj, param);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignore
            }

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
                String split = SPLIT + SPACE;
                for (Enum em : constants) {
                    Object code = getMethod(em, "getCode");
                    if (isNotBlank(code)) {
                        // has getCode
                        sbd.append(code).append(":");
                        Object value = getMethod(em, "getValue");
                        // has getValue return <code1: value1, code2: value2 ...>
                        // no getValue return <code1: name1, code2: name2 ...>
                        sbd.append(isNotBlank(value) ? value : em.name());
                    } else {
                        // no getCode return <name1, name2>
                        // sbd.append(em.ordinal()).append(":");
                        sbd.append(em.name());
                    }
                    sbd.append(split);
                }
                int len = split.length();
                if (sbd.length() > len) {
                    return sbd.delete(sbd.length() - len, sbd.length()).toString();
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
        // include enum
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
    static Object getReturnType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        else if (clazz == boolean.class || clazz == Boolean.class) {
            return false;
        } else if (clazz == boolean[].class || clazz == Boolean[].class) {
            return new boolean[] { false };
        }

        else if (clazz == byte.class || clazz == Byte.class) {
            return (byte) 0;
        } else if (clazz == byte[].class || clazz == Byte[].class) {
            return new byte[] { (byte) 0 };
        }

        else if (clazz == char.class || clazz == Character.class) {
            return (char) 0;
        } else if (clazz == char[].class || clazz == Character[].class) {
            return new char[] { (char) 0 };
        }

        else if (clazz == short.class || clazz == Short.class) {
            return (short) 0;
        } else if (clazz == short[].class || clazz == Short[].class) {
            return new short[] { (short) 0 };
        }

        else if (clazz == int.class || clazz == Integer.class) {
            return 0;
        } else if (clazz == int[].class || clazz == Integer[].class) {
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

        else if (clazz == double.class || clazz == Double.class) {
            return 0D;
        } else if (clazz == double[].class || clazz == Double[].class) {
            return new double[] { 0D };
        }

        else if (clazz == BigDecimal.class) {
            return new BigDecimal(0D);
        } else if (clazz == BigDecimal[].class) {
            return new BigDecimal[] { new BigDecimal(0D) };
        }

        else if (clazz == String.class) {
            return Tools.EMPTY;
        } else if (clazz == String[].class) {
            return new String[] { Tools.EMPTY };
        }

        else if (clazz.isEnum()) {
            // 类型如果是枚举, 则拿第一个进行返回
            Object[] enumConstants = clazz.getEnumConstants();
            return (enumConstants.length > 0) ? enumConstants[0] : null;
        }

        else {
            return null;
        }
    }

    // ========== mvc ==========
    public static String getDomain() {
        HttpServletRequest request = getRequestAttributes().getRequest();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        if (port < 0) {
            port = 80;
        }

        StringBuilder sbd = new StringBuilder();
        sbd.append(scheme).append("://").append(request.getServerName());
        boolean http = "http".equalsIgnoreCase(scheme) && (port != 80);
        if (http || ("https".equalsIgnoreCase(scheme) && (port != 443))) {
            sbd.append(':').append(port);
        }
        return sbd.toString();
    }

    private static ServletRequestAttributes getRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    }
}
