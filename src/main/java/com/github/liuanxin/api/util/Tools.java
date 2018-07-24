package com.github.liuanxin.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    public static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String SPLIT = ",";

    private static final String FILE_TYPE = "file";

    /** just basic type: int long double array etc... */
    private static final Map<String, Class<?>> BASIC_CLAZZ_MAP = maps(
            boolean.class.getSimpleName(), boolean.class,
            Boolean.class.getSimpleName(), boolean.class,

            byte.class.getSimpleName(), byte.class,
            Byte.class.getSimpleName(), byte.class,

            char.class.getSimpleName(), char.class,
            Character.class.getSimpleName(), char.class,

            short.class.getSimpleName(), short.class,
            Short.class.getSimpleName(), short.class,

            int.class.getSimpleName(), int.class,
            Integer.class.getSimpleName(), int.class,

            long.class.getSimpleName(), long.class,
            Long.class.getSimpleName(), long.class,

            float.class.getSimpleName(), float.class,
            Float.class.getSimpleName(), float.class,

            double.class.getSimpleName(), double.class,
            Double.class.getSimpleName(), double.class,

            // up type, down array type

            boolean[].class.getSimpleName(), boolean[].class,
            Boolean[].class.getSimpleName(), boolean[].class,

            byte[].class.getSimpleName(), byte[].class,
            Byte[].class.getSimpleName(), byte[].class,

            char[].class.getSimpleName(), char[].class,
            Character[].class.getSimpleName(), char[].class,

            short[].class.getSimpleName(), short[].class,
            Short[].class.getSimpleName(), short[].class,

            int[].class.getSimpleName(), int[].class,
            Integer[].class.getSimpleName(), int[].class,

            long[].class.getSimpleName(), long[].class,
            Long[].class.getSimpleName(), long[].class,

            float[].class.getSimpleName(), float[].class,
            Float[].class.getSimpleName(), float[].class,

            double[].class.getSimpleName(), double[].class,
            Double[].class.getSimpleName(), double[].class
    );

    /** { basic-type : default-value } */
    private static final Map<String, Object> BASIC_TYPE_VALUE_MAP = maps(
            boolean.class.getSimpleName(), false,
            Boolean.class.getSimpleName(), false,

            byte.class.getSimpleName(), (byte) 0,
            Byte.class.getSimpleName(), (byte) 0,

            char.class.getSimpleName(), (char) 0,
            Character.class.getSimpleName(), (char) 0,

            short.class.getSimpleName(), (short) 0,
            Short.class.getSimpleName(), (short) 0,

            int.class.getSimpleName(), 0,
            Integer.class.getSimpleName(), 0,

            long.class.getSimpleName(), 0L,
            Long.class.getSimpleName(), 0L,

            BigInteger.class.getSimpleName(), BigInteger.ZERO,

            float.class.getSimpleName(), 0F,
            Float.class.getSimpleName(), 0F,

            double.class.getSimpleName(), 0D,
            Double.class.getSimpleName(), 0D,

            BigDecimal.class.getSimpleName(), BigDecimal.ZERO,
            String.class.getSimpleName(), EMPTY,

            // up type, down array type

            boolean[].class.getSimpleName(), new boolean[] { false },
            Boolean[].class.getSimpleName(), new boolean[] { false },

            byte[].class.getSimpleName(), new byte[] { (byte) 0 },
            Byte[].class.getSimpleName(), new byte[] { (byte) 0 },

            char[].class.getSimpleName(), new char[] { (char) 0 },
            Character[].class.getSimpleName(), new char[] { (char) 0 },

            short[].class.getSimpleName(), new short[] { (short) 0 },
            Short[].class.getSimpleName(), new short[] { (short) 0 },

            int[].class.getSimpleName(), new int[] { 0 },
            Integer[].class.getSimpleName(), new int[] { 0 },

            long[].class.getSimpleName(), new long[] { 0L },
            Long[].class.getSimpleName(), new long[] { 0L },

            BigInteger[].class.getSimpleName(), new BigInteger[] { BigInteger.ZERO },

            float[].class.getSimpleName(), new float[] { 0F },
            Float[].class.getSimpleName(), new float[] { 0F },

            double[].class.getSimpleName(), new double[] { 0D },
            Double[].class.getSimpleName(), new double[] { 0D },

            BigDecimal[].class.getSimpleName(), new BigDecimal[] { BigDecimal.ZERO },
            String[].class.getSimpleName(), new String[] { EMPTY }
    );

    // ========== string ==========
    public static boolean isBlankObj(Object obj) {
        return obj == null;
    }
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

    private static final ObjectWriter PRETTY_RENDER = RENDER.writerWithDefaultPrettyPrinter();
    public static String toJson(Object obj) {
        if (isBlank(obj)) {
            return EMPTY;
        }
        try {
            return RENDER.writeValueAsString(obj);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("obj(%s) to json exception", obj.toString()), e);
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
                LOGGER.error(String.format("str(%s) to pretty json exception", json), e);
            }
            return EMPTY;
        }
    }

    // ========== array map ==========
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
    public static <T> boolean isNotEmpty(T[] array) {
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
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }
    public static <K, V> HashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> maps(Object... keysAndValues) {
        return (HashMap<K, V>) maps(newHashMap(), keysAndValues);
    }
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> maps(Map<K, V> result, Object... keysAndValues) {
        if (isNotEmpty(keysAndValues)) {
            for (int i = 0; i < keysAndValues.length; i += 2) {
                if (keysAndValues.length > (i + 1)) {
                    result.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
                }
            }
        }
        return result;
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
    public static String enumInfo(Class<?> clazz) {
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
    public static Class<?> getBasicType(String name) {
        return BASIC_CLAZZ_MAP.get(name);
    }

    private static Object getTypeDefaultValue(Class<?> clazz) {
        return BASIC_TYPE_VALUE_MAP.get(clazz.getSimpleName());
    }
    public static boolean basicType(Class<?> clazz) {
        if (isBlankObj(clazz)) {
            return false;
        }
        Object defaultValue = getTypeDefaultValue(clazz);
        if (!isBlankObj(defaultValue)) {
            return true;
        }
        // include enum
        return clazz.isEnum();
    }
    public static boolean notBasicType(Class<?> clazz) {
        return !basicType(clazz);
    }

    public static boolean hasFileInput(String fileType) {
        return FILE_TYPE.equals(fileType);
    }
    public static String getInputType(Class<?> type) {
        if (isBlankObj(type)) {
            return EMPTY;
        }

        // upload file
        if (MultipartFile.class.isAssignableFrom(type)) {
            return FILE_TYPE;
        }
        String paramType = type.getSimpleName();
        // basic type
        Class<?> basicClass = getBasicType(paramType);
        if (!isBlankObj(basicClass)) {
            return basicClass.getSimpleName();
        }
        // enum
        if (type.isEnum()) {
            return "Enum(" + paramType + ")";
        }
        // string, string[], list, set, map... etc
        return paramType.substring(0, 1).toLowerCase() + paramType.substring(1);
    }

    public static Object getReturnType(Class<?> clazz) {
        if (isBlankObj(clazz)) {
            return null;
        }

        Object defaultValue = getTypeDefaultValue(clazz);
        if (!isBlankObj(defaultValue)) {
            return defaultValue;
        } else if (clazz.isEnum()) {
            // Enum return first
            Object[] enumConstants = clazz.getEnumConstants();
            return (enumConstants.length > 0) ? enumConstants[0] : null;
        } else {
            return null;
        }
    }
}
