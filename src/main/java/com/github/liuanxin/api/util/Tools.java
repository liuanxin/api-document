package com.github.liuanxin.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.liuanxin.api.constant.ApiConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    /** enum info */
    private static final Map<String, Object> ENUM_MAP = maps();

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

            BigInteger.class.getSimpleName(), long.class,
            BigDecimal.class.getSimpleName(), double.class,
            String.class.getSimpleName(), String.class,
            Date.class.getSimpleName(), Date.class,

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
            Double[].class.getSimpleName(), double[].class,

            BigInteger[].class.getSimpleName(), long[].class,
            BigDecimal[].class.getSimpleName(), double[].class,
            String[].class.getSimpleName(), String[].class,
            Date[].class.getSimpleName(), Date[].class
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

            float.class.getSimpleName(), 0F,
            Float.class.getSimpleName(), 0F,

            double.class.getSimpleName(), 0D,
            Double.class.getSimpleName(), 0D,

            BigInteger.class.getSimpleName(), BigInteger.ZERO,
            BigDecimal.class.getSimpleName(), BigDecimal.ZERO,
            String.class.getSimpleName(), ApiConst.EMPTY,
            Date.class.getSimpleName(), ApiConst.NOW,

            // up type, down array type

            boolean[].class.getSimpleName(), new boolean[] { false },
            Boolean[].class.getSimpleName(), new Boolean[] { false },

            byte[].class.getSimpleName(), new byte[] { (byte) 0 },
            Byte[].class.getSimpleName(), new Byte[] { (byte) 0 },

            char[].class.getSimpleName(), new char[] { (char) 0 },
            Character[].class.getSimpleName(), new Character[] { (char) 0 },

            short[].class.getSimpleName(), new short[] { (short) 0 },
            Short[].class.getSimpleName(), new Short[] { (short) 0 },

            int[].class.getSimpleName(), new int[] { 0 },
            Integer[].class.getSimpleName(), new Integer[] { 0 },

            long[].class.getSimpleName(), new long[] { 0L },
            Long[].class.getSimpleName(), new Long[] { 0L },

            float[].class.getSimpleName(), new float[] { 0F },
            Float[].class.getSimpleName(), new Float[] { 0F },

            double[].class.getSimpleName(), new double[] { 0D },
            Double[].class.getSimpleName(), new Double[] { 0D },

            BigInteger[].class.getSimpleName(), new BigInteger[] { BigInteger.ZERO },
            BigDecimal[].class.getSimpleName(), new BigDecimal[] { BigDecimal.ZERO },
            String[].class.getSimpleName(), new String[] { ApiConst.EMPTY },
            Date[].class.getSimpleName(), new Date[] { ApiConst.NOW }
    );

    private static final Map<String, Object> DEFAULT_MAP_KEY = maps(
            Boolean.class.getSimpleName(), false,
            Byte.class.getSimpleName(), (byte) 0,
            Character.class.getSimpleName(), (char) 0,
            Short.class.getSimpleName(), (short) 0,
            Integer.class.getSimpleName(), 0,
            Long.class.getSimpleName(), 0L,
            Float.class.getSimpleName(), 0F,
            Double.class.getSimpleName(), 0D,
            BigInteger.class.getSimpleName(), BigInteger.ZERO,
            BigDecimal.class.getSimpleName(), BigDecimal.ZERO,
            String.class.getSimpleName(), "?"
    );

    // ========== string ==========
    public static boolean isBlank(Object obj) {
        return obj == null;
    }
    public static boolean isNotBlank(Object obj) {
        return !isBlank(obj);
    }
    public static boolean isEmpty(Object obj) {
        return isBlank(obj) || ApiConst.EMPTY.equals(obj.toString().trim());
    }
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
    private static String toStr(Object obj) {
        return isBlank(obj) ? ApiConst.EMPTY : obj.toString();
    }

    // ========== date ==========
    private static Date parseDate(String source) {
        if (isNotBlank(source)) {
            source = source.trim();
            for (DateType type : DateType.values()) {
                try {
                    Date date = new SimpleDateFormat(type.getValue()).parse(source);
                    if (Tools.isNotBlank(date)) {
                        return date;
                    }
                } catch (ParseException | IllegalArgumentException ignore) {
                }
            }
        }
        return ApiConst.NOW;
    }

    // ========== json ==========
    private static final ObjectMapper RENDER = new ObjectMapper();

    private static final ObjectWriter PRETTY_RENDER = RENDER.writerWithDefaultPrettyPrinter();
    public static String toJson(Object obj) {
        if (isEmpty(obj)) {
            return ApiConst.EMPTY;
        }
        try {
            return RENDER.writeValueAsString(obj);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("obj(%s) to json exception", obj.toString()), e);
            }
            return ApiConst.EMPTY;
        }
    }
    public static <T> T toObject(String json, Class<T> clazz) {
        if (isEmpty(json)) {
            return null;
        }
        try {
            return RENDER.readValue(json, clazz);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("json(%s) to Object(%s) exception", json, clazz.getName()), e);
            }
            return null;
        }
    }
    public static String toPrettyJson(String json) {
        if (isEmpty(json)) {
            return ApiConst.EMPTY;
        }
        try {
            return PRETTY_RENDER.writeValueAsString(RENDER.readValue(json, Object.class));
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("str(%s) to pretty json exception", json), e);
            }
            return ApiConst.EMPTY;
        }
    }

    // ========== array map ==========
    public static <T> boolean isEmpty(T[] array) {
        return isBlank(array) || array.length == 0;
    }
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }
    public static <T> boolean isEmpty(Collection<T> collection) {
        return isBlank(collection) || collection.isEmpty();
    }
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
    private static boolean isEmpty(Map map) {
        return isBlank(map) || map.isEmpty();
    }
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }
    public static <T> T first(T[] array) {
        return isEmpty(array) ? null : array[0];
    }
    public static <T> String toStr(Collection<T> collection) {
        if (isEmpty(collection)) {
            return ApiConst.EMPTY;
        }
        StringBuilder sbd = new StringBuilder();
        int i = 0;
        for (T t : collection) {
            sbd.append(t);
            if (i + 1 != collection.size()) {
                sbd.append(ApiConst.SPLIT);
            }
            i++;
        }
        return sbd.toString();
    }

    // ========== list map ==========
    @SuppressWarnings("unchecked")
    static List lists(Object... values) {
        return isEmpty(values) ? new ArrayList() : new ArrayList(Arrays.asList(values));
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
    private static Object getMethod(Object obj, String method) {
        if (isNotEmpty(method)) {
            try {
                return obj.getClass().getDeclaredMethod(method).invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignore
            }

            try {
                return obj.getClass().getMethod(method).invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                // ignore
            }
        }
        return null;
    }

    // ========== enum ==========
    private static Object toEnum(Class<?> clazz, Object obj) {
        if (isNotBlank(clazz) && clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            if (isNotEmpty(constants)) {
                if (isNotEmpty(obj)) {
                    String source = obj.toString().trim();
                    for (Object em : constants) {
                        if (source.equalsIgnoreCase(((Enum) em).name())) {
                            return em;
                        }
                        Object code = getMethod(em, "getCode");
                        if (isNotBlank(code) && source.equalsIgnoreCase(code.toString().trim())) {
                            return em;
                        }
                        Object value = getMethod(em, "getValue");
                        if (isNotBlank(value) && source.equalsIgnoreCase(value.toString().trim())) {
                            return em;
                        }
                        /*
                        if (source.equalsIgnoreCase(String.valueOf(((Enum) em).ordinal()))) {
                            return em;
                        }
                        */
                    }
                }
                return constants[0];
            }
        }
        return null;
    }
    static String descInfo(Class<?> clazz, String desc) {
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        // enum append (code:value)
        String enumInfo = collectEnumInfo(clazz);
        if (isNotEmpty(enumInfo)) {
            return isEmpty(desc) ? enumInfo : String.format("%s(%s)", desc, enumInfo);
        } else {
            return isEmpty(desc) ? ApiConst.EMPTY : desc;
        }
    }
    private static String collectEnumInfo(Class<?> clazz) {
        if (isNotBlank(clazz) && clazz.isEnum()) {
            Enum[] constants = (Enum[]) clazz.getEnumConstants();
            if (isNotEmpty(constants)) {
                StringBuilder sbd = new StringBuilder();
                String split = ApiConst.SPLIT + ApiConst.SPACE;
                // enum info, maybe Map or maybe List
                Map<String, Object> map = newHashMap();
                List<String> list = lists();
                for (Enum em : constants) {
                    // has getCode
                    Object code = getMethod(em, "getCode");
                    String name = em.name();
                    if (isNotEmpty(code)) {
                        String key = toStr(code);
                        Object value = getMethod(em, "getValue");
                        // has getValue return <code1: value1, code2: value2 ...>
                        // no getValue return <code1: name1, code2: name2 ...>
                        Object obj = isNotEmpty(value) ? value : name;
                        sbd.append(key).append(":").append(obj);
                        map.put(key, obj);
                    } else {
                        // no getCode return <name1, name2>
                        // sbd.append(em.ordinal()).append(":");
                        sbd.append(name);
                        list.add(name);
                    }
                    sbd.append(split);
                }
                // just one rule, mix rule will use <code1: (value1|name1), code2: (value2|name2)>
                ENUM_MAP.put(clazz.getSimpleName(), (isEmpty(map) ? list : map));

                int len = split.length();
                if (sbd.length() > len) {
                    return sbd.delete(sbd.length() - len, sbd.length()).toString();
                }
            }
        }
        return ApiConst.EMPTY;
    }
    public static Map<String, Object> allEnumInfo() {
        return ENUM_MAP;
    }

    // ========== type ==========
    static Class<?> getBasicType(String name) {
        return BASIC_CLAZZ_MAP.get(name);
    }

    static boolean hasInDepth(String showDataType, Class<?> parameterType) {
        if (!notShowByBasicType(showDataType)) {
            return false;
        }
        if (basicType(parameterType)) {
            return false;
        }
        return !MultipartFile.class.isAssignableFrom(parameterType)
                    && !Collection.class.isAssignableFrom(parameterType)
                    && !Map.class.isAssignableFrom(parameterType);
    }
    private static boolean notShowByBasicType(String dataType) {
        if (isNotEmpty(dataType)) {
            for (String key : BASIC_TYPE_VALUE_MAP.keySet()) {
                if (key.equalsIgnoreCase(dataType)) {
                    return false;
                }
            }
        }
        return true;
    }
    private static Object getTypeDefaultValue(Class<?> clazz) {
        return BASIC_TYPE_VALUE_MAP.get(clazz.getSimpleName());
    }
    static boolean basicType(Class<?> clazz) {
        if (isBlank(clazz)) {
            return false;
        }
        Object defaultValue = getTypeDefaultValue(clazz);
        if (!isBlank(defaultValue)) {
            return true;
        }
        // include enum
        return clazz.isEnum();
    }
    static boolean notBasicType(Class<?> clazz) {
        return !basicType(clazz);
    }

    static Object mapKeyDefault(Class<?> clazz) {
        Object keyDefault = DEFAULT_MAP_KEY.get(clazz.getSimpleName());
        if (isNotEmpty(keyDefault)) {
            return keyDefault;
        } else if (clazz.isEnum()) {
            return first(clazz.getEnumConstants());
        } else {
            return null;
        }
    }

    static boolean hasFileInput(String fileType) {
        return ApiConst.FILE_TYPE.equals(fileType);
    }
    static String getInputType(Class<?> type) {
        if (isBlank(type)) {
            return ApiConst.EMPTY;
        }
        if (type.isArray()) {
            return getType(type.getComponentType()) + "[]";
        } else {
            return getType(type);
        }
    }
    private static String getType(Class<?> type) {
        // upload file
        if (MultipartFile.class.isAssignableFrom(type)) {
            return ApiConst.FILE_TYPE;
        }
        String paramType = type.getSimpleName();
        Class<?> basicClass = getBasicType(paramType);
        if (isNotEmpty(basicClass)) {
            return basicClass.getSimpleName();
        } else if (type.isEnum()) {
            return "enum(" + paramType + ")";
        } else {
            // string, string[], list, set, map... etc
            return paramType.substring(0, 1).toLowerCase() + paramType.substring(1);
        }
    }

    static Object getReturnType(Class<?> clazz) {
        if (isBlank(clazz)) {
            return null;
        }

        Object defaultValue = getTypeDefaultValue(clazz);
        if (isNotBlank(defaultValue)) {
            return defaultValue;
        } else if (clazz.isEnum()) {
            // Enum return first
            return first(clazz.getEnumConstants());
        } else {
            return null;
        }
    }

    private static final List<String> TRUE_LIST = Arrays.asList("true", "on", "yes", "1", "✓", "✔", "☑");
    static Object getReturnTypeExample(Class<?> clazz, String example) {
        if (isBlank(clazz)) {
            return null;
        }

        if (clazz.isEnum()) {
            return toEnum(clazz, example);
        }
        Object defaultObj = getTypeDefaultValue(clazz);
        if (isBlank(example)) {
            return defaultObj;
        }

        if (clazz == String.class) {
            return isEmpty(example) ? defaultObj.toString() : example;
        }
        else if (clazz == boolean.class || clazz == Boolean.class) {
            return TRUE_LIST.contains(example.trim().toLowerCase());
        }
        else if (clazz == byte.class || clazz == Byte.class) {
            try {
                return isHexNumber(example) ? Byte.decode(example) : Byte.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == char.class || clazz == Character.class) {
            try {
                return example.charAt(0);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == short.class || clazz == Short.class) {
            try {
                return isHexNumber(example) ? Short.decode(example) : Short.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == int.class || clazz == Integer.class) {
            try {
                return isHexNumber(example) ? Integer.decode(example) : Integer.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == long.class || clazz == Long.class) {
            try {
                return isHexNumber(example) ? Long.decode(example) : Long.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == float.class || clazz == Float.class) {
            try {
                return Float.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == double.class || clazz == Double.class) {
            try {
                return Double.valueOf(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == Date.class) {
            return Tools.parseDate(example);
        }
        else if (clazz == BigInteger.class) {
            try {
                return isHexNumber(example) ? decodeBigInteger(example) : new BigInteger(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == BigDecimal.class) {
            try {
                return new BigDecimal(example);
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        // up type, down array type

        else if (clazz == String[].class) {
            String str = isEmpty(example) ? defaultObj.toString() : example;
            return new String[] { str };
        }

        else if (clazz == boolean[].class) {
            return new boolean[] { TRUE_LIST.contains(example.trim().toLowerCase()) };
        } else if (clazz == Boolean[].class) {
            return new Boolean[] { TRUE_LIST.contains(example.trim().toLowerCase()) };
        }

        else if (clazz == byte[].class) {
            try {
                return new byte[] { isHexNumber(example) ? Byte.decode(example) : Byte.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Byte[].class) {
            try {
                return new Byte[] { isHexNumber(example) ? Byte.decode(example) : Byte.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == char[].class) {
            try {
                return new char[] { example.charAt(0) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Character[].class) {
            try {
                return new Character[] { example.charAt(0) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == short[].class) {
            try {
                return new short[] { isHexNumber(example) ? Short.decode(example) : Short.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Short[].class) {
            try {
                return new Short[] { isHexNumber(example) ? Short.decode(example) : Short.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == int[].class) {
            try {
                return new int[] { isHexNumber(example) ? Integer.decode(example) : Integer.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Integer[].class) {
            try {
                return new Integer[] { isHexNumber(example) ? Integer.decode(example) : Integer.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == long[].class) {
            try {
                return new long[] { isHexNumber(example) ? Long.decode(example) : Long.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Long[].class) {
            try {
                return new Long[] { isHexNumber(example) ? Long.decode(example) : Long.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == float[].class) {
            try {
                return new float[] { Float.parseFloat(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Float[].class) {
            try {
                return new Float[] { Float.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == double[].class) {
            try {
                return new double[] { Double.parseDouble(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        } else if (clazz == Double[].class) {
            try {
                return new Double[] { Double.valueOf(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else if (clazz == Date[].class) {
            return new Date[] { Tools.parseDate(example) };
        }

        else if (clazz == BigInteger[].class) {
            try {
                return new BigInteger[] { isHexNumber(example) ? decodeBigInteger(example) : new BigInteger(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }
        else if (clazz == BigDecimal[].class) {
            try {
                return new BigDecimal[] { new BigDecimal(example) };
            } catch (NumberFormatException e) {
                return defaultObj;
            }
        }

        else {
            return defaultObj;
        }
    }

    private static boolean isHexNumber(String value) {
        int index = value.startsWith("-") ? 1 : 0;
        return value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index);
    }
    private static BigInteger decodeBigInteger(String value) {
        int radix;
        int index = 0;

        boolean negative;
        if (value.startsWith("-")) {
            negative = true;
            index++;
        } else {
            negative = false;
        }

        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (value.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (value.startsWith("0", index) && value.length() > 1 + index) {
            index++;
            radix = 8;
        } else {
            radix = 10;
        }
        BigInteger result = new BigInteger(value.substring(index), radix);
        return (negative ? result.negate() : result);
    }
}
