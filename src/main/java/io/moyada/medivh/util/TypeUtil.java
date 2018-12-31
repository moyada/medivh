package io.moyada.medivh.util;

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

    public static final char UNKNOWN = '-';

    private static final char BYTE = 'B';
    private static final char SHORT = 'S';
    private static final char INTEGER = 'I';
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

        MIN.put(INTEGER, Integer.MIN_VALUE);
        MAX.put(INTEGER, Integer.MAX_VALUE);

        MIN.put(LONG, Long.MIN_VALUE);
        MAX.put(LONG, Long.MAX_VALUE);

        MIN.put(FLOAT, Float.MIN_VALUE);
        MAX.put(FLOAT, Float.MAX_VALUE);

        MIN.put(DOUBLE, Double.MIN_VALUE);
        MAX.put(DOUBLE, Double.MAX_VALUE);
    }

    /**
     * 是否基本类型
     * @param name
     * @return
     */
    public static boolean isPrimitive(String name) {
        if (name.equals("byte")) {
            return true;
        }
        if (name.equals("short")) {
            return true;
        }
        if (name.equals("int")) {
            return true;
        }
        if (name.equals("long")) {
            return true;
        }
        if (name.equals("char")) {
            return true;
        }
        if (name.equals("float")) {
            return true;
        }
        if (name.equals("double")) {
            return true;
        }
        if (name.equals("boolean")) {
            return true;
        }
        return false;
    }

    /**
     * 是否字符串
     * @param name
     * @return
     */
    public static boolean isStr(String name) {
        return name.equals("java.lang.String");
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
     * 是否数字类型
     * @param name
     * @return
     */
    public static char getNumType(String name) {
        if (name.equals("byte") || name.equals("java.lang.Byte")) {
            return BYTE;
        }
        if (name.equals("short") || name.equals("java.lang.Short")) {
            return SHORT;
        }
        if (name.equals("int") || name.equals("java.lang.Integer")) {
            return INTEGER;
        }
        if (name.equals("long") || name.equals("java.lang.Long")) {
            return LONG;
        }
        if (name.equals("float") || name.equals("java.lang.Float")) {
            return FLOAT;
        }
        if (name.equals("double") || name.equals("java.lang.Double")) {
            return DOUBLE;
        }
        return UNKNOWN;
    }

    /**
     * 返回基本类型
     * @param name
     * @return
     */
    public static TypeTag getBaseType(String name) {
        if (name.equals("byte") || name.equals("java.lang.Byte")) {
            return TypeTag.BYTE;
        }
        if (name.equals("short") || name.equals("java.lang.Short")) {
            return TypeTag.SHORT;
        }
        if (name.equals("int") || name.equals("java.lang.Integer")) {
            return TypeTag.INT;
        }
        if (name.equals("long") || name.equals("java.lang.Long")) {
            return TypeTag.LONG;
        }
        if (name.equals("float") || name.equals("java.lang.Float")) {
            return TypeTag.FLOAT;
        }
        if (name.equals("double") || name.equals("java.lang.Double")) {
            return TypeTag.DOUBLE;
        }
        if (name.equals("boolean") || name.equals("java.lang.Boolean")) {
            return TypeTag.BOOLEAN;
        }
        if (name.equals("char") || name.equals("java.lang.Character")) {
            return TypeTag.CHAR;
        }
        if (name.equals("java.lang.String")) {
            return TypeTag.CLASS;
        }
        return null;
    }

    public static TypeTag getTypeTag(char type) {
        TypeTag typeTag;
        switch (type) {
            case BYTE:
                typeTag = TypeTag.BYTE;
                break;
            case SHORT:
                typeTag = TypeTag.SHORT;
                break;
            case INTEGER:
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
                case INTEGER:
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

    public static int compare(char type, Object o1, Object o2) {
        Number m1 = (Number) o1;
        Number m2 = (Number) o2;
        switch (type) {
            case BYTE:
                return m1.byteValue() < m2.byteValue() ? -1 : m1.byteValue() > m2.byteValue() ? 1 : 0;
            case SHORT:
                return m1.shortValue() < m2.shortValue() ? -1 : m1.shortValue() > m2.shortValue() ? 1 : 0;
            case INTEGER:
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
