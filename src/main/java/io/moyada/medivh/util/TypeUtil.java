package io.moyada.medivh.util;

import io.moyada.medivh.core.TypeTag;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型工具
 * @author xueyikang
 * @since 1.0
 **/
public final class TypeUtil {

    private TypeUtil() {
    }

    public final static byte PRIMITIVE = -1;

    public final static byte STRING = 0;
    public final static byte ARRAY = 1;
    public final static byte COLLECTION = 2;
    public final static byte OBJECT = 3;

    public static final char UNKNOWN = '-';

    private static final char BYTE = 'B';
    private static final char SHORT = 'S';
    private static final char INT = 'I';
    private static final char LONG = 'L';
    private static final char FLOAT = 'F';
    private static final char DOUBLE = 'D';

    private static final Map<Character, Object> MIN = new HashMap<Character, Object>();
    private static final Map<Character, Object> MAX = new HashMap<Character, Object>();

    static {
        MIN.put(BYTE, Byte.MIN_VALUE);
        MAX.put(BYTE, Byte.MAX_VALUE);

        MIN.put(SHORT, Short.MIN_VALUE);
        MAX.put(SHORT, Short.MAX_VALUE);

        MIN.put(INT, Integer.MIN_VALUE);
        MAX.put(INT, Integer.MAX_VALUE);

        MIN.put(LONG, Long.MIN_VALUE);
        MAX.put(LONG, Long.MAX_VALUE);

        MIN.put(FLOAT, Float.MIN_VALUE);
        MAX.put(FLOAT, Float.MAX_VALUE);

        MIN.put(DOUBLE, Double.MIN_VALUE);
        MAX.put(DOUBLE, Double.MAX_VALUE);
    }

    /**
     * 是否原始类型
     * @param name
     * @return
     */
    public static boolean isPrimitive(String name) {
        if (name.equals(getName(byte.class))) {
            return true;
        }
        if (name.equals(getName(short.class))) {
            return true;
        }
        if (name.equals(getName(int.class))) {
            return true;
        }
        if (name.equals(getName(long.class))) {
            return true;
        }
        if (name.equals(getName(float.class))) {
            return true;
        }
        if (name.equals(getName(double.class))) {
            return true;
        }
        if (name.equals(getName(boolean.class))) {
            return true;
        }
        if (name.equals(getName(char.class))) {
            return true;
        }
        return false;
    }

    /**
     * 获取包装类型
     * @param name
     * @return
     */
    public static String getWrapperType(String name) {
        if (name.equals(getName(byte.class))) {
            return Byte.class.getName();
        }
        if (name.equals(getName(short.class))) {
            return Short.class.getName();
        }
        if (name.equals(getName(int.class))) {
            return Integer.class.getName();
        }
        if (name.equals(getName(long.class))) {
            return Long.class.getName();
        }
        if (name.equals(getName(char.class))) {
            return Character.class.getName();
        }
        if (name.equals(getName(float.class))) {
            return Float.class.getName();
        }
        if (name.equals(getName(double.class))) {
            return Double.class.getName();
        }
        if (name.equals(getName(boolean.class))) {
            return Boolean.class.getName();
        }
        return name;
    }

    /**
     * 获取类名
     * @param clazz
     * @return
     */
    private static String getName(Class<?> clazz) {
        return clazz.getName();
    }

    /**
     * 是否字符串
     * @param name
     * @return
     */
    public static boolean isStr(String name) {
        return name.equals(getName(String.class));
    }

    /**
     * 是否数组
     * @param name
     * @return
     */
    public static boolean isArr(String name) {
        return name.equals("Array") || name.endsWith("[]");
    }

    /**
     * 返回数字类型标记
     * @param name
     * @return
     */
    public static char getNumType(String name) {
        if (name.equals(getName(byte.class)) || name.equals(getName(Byte.class))) {
            return BYTE;
        }
        if (name.equals(getName(short.class)) || name.equals(getName(Short.class))) {
            return SHORT;
        }
        if (name.equals(getName(int.class)) || name.equals(getName(Integer.class))) {
            return INT;
        }
        if (name.equals(getName(long.class)) || name.equals(getName(Long.class))) {
            return LONG;
        }
        if (name.equals(getName(float.class)) || name.equals(getName(Float.class))) {
            return FLOAT;
        }
        if (name.equals(getName(double.class)) || name.equals(getName(Double.class))) {
            return DOUBLE;
        }
        return UNKNOWN;
    }

    /**
     * 返回基本类型标签
     * @param name
     * @return
     */
    public static TypeTag getBaseType(String name) {
        if (name.equals(getName(byte.class)) || name.equals(getName(Byte.class))) {
            return TypeTag.BYTE;
        }
        if (name.equals(getName(short.class)) || name.equals(getName(Short.class))) {
            return TypeTag.SHORT;
        }
        if (name.equals(getName(int.class)) || name.equals(getName(Integer.class))) {
            return TypeTag.INT;
        }
        if (name.equals(getName(long.class)) || name.equals(getName(Long.class))) {
            return TypeTag.LONG;
        }
        if (name.equals(getName(float.class)) || name.equals(getName(Float.class))) {
            return TypeTag.FLOAT;
        }
        if (name.equals(getName(double.class)) || name.equals(getName(Double.class))) {
            return TypeTag.DOUBLE;
        }
        if (name.equals(getName(boolean.class)) || name.equals(getName(Boolean.class))) {
            return TypeTag.BOOLEAN;
        }
        if (name.equals(getName(char.class)) || name.equals(getName(Character.class))) {
            return TypeTag.CHAR;
        }
        if (isStr(name)) {
            return TypeTag.CLASS;
        }
        return null;
    }

    /**
     * 根据标记获取标签
     * @param type
     * @return
     */
    public static TypeTag getTypeTag(char type) {
        TypeTag typeTag;
        switch (type) {
            case BYTE:
                typeTag = TypeTag.BYTE;
                break;
            case SHORT:
                typeTag = TypeTag.SHORT;
                break;
            case INT:
                typeTag = TypeTag.INT;
                break;
            case LONG:
                typeTag = TypeTag.LONG;
                break;
            case FLOAT:
                typeTag = TypeTag.FLOAT;
                break;
            case DOUBLE:
                typeTag = TypeTag.DOUBLE;
                break;
            default:
                typeTag = null;
        }

        return typeTag;
    }

    /**
     * 转换数值
     * @param type
     * @param input
     * @return
     */
    static Object getNumberValue(char type, String input) {
        if (null == input) {
            return null;
        }

        Object value;
        try {
            switch (type) {
                case BYTE:
                    value = Byte.valueOf(input);
                    break;
                case SHORT:
                    value = Short.valueOf(input);
                    break;
                case INT:
                    value = Integer.valueOf(input);
                    break;
                case LONG:
                    value = Long.valueOf(input);
                    break;
                case FLOAT:
                    value = Float.valueOf(input);
                    break;
                case DOUBLE:
                    value = Double.valueOf(input);
                    break;
                default:
                    value = null;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return value;
    }

    /**
     * 获取最小数
     * @param type
     * @param input
     * @return
     */
    public static Object getMin(char type, String input) {
        Object value = getNumberValue(type, input);
        if (null == value) {
            return null;
        }

        Object min = MIN.get(type);
        int result = compare(type, min, value);
        if (result == -1) {
            return value;
        }
        return null;
    }

    /**
     * 获取最大数
     * @param type
     * @param input
     * @return
     */
    public static Object getMax(char type, String input) {
        Object value = getNumberValue(type, input);
        if (null == value) {
            return null;
        }

        Object max = MAX.get(type);
        int result = compare(type, value, max);
        if (result == -1) {
            return value;
        }
        return null;
    }

    /**
     * 比较转换数值
     * @param type
     * @param o1
     * @param o2
     * @return
     */
    public static int compare(char type, Object o1, Object o2) {
        Number m1 = (Number) o1;
        Number m2 = (Number) o2;
        switch (type) {
            case BYTE:
                return m1.byteValue() < m2.byteValue() ? -1 : m1.byteValue() > m2.byteValue() ? 1 : 0;
            case SHORT:
                return m1.shortValue() < m2.shortValue() ? -1 : m1.shortValue() > m2.shortValue() ? 1 : 0;
            case INT:
                return m1.intValue() < m2.intValue() ? -1 : m1.intValue() > m2.intValue() ? 1 : 0;
            case LONG:
                return m1.longValue() < m2.longValue() ? -1 : m1.longValue() > m2.longValue() ? 1 : 0;
            case FLOAT:
                return Float.compare(m1.floatValue(), Math.abs(m2.floatValue()));
            case DOUBLE:
                return Double.compare(m1.doubleValue(), Math.abs(m2.doubleValue()));
        }
        return 0;
    }
}
