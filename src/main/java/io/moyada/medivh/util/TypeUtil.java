package io.moyada.medivh.util;

import io.moyada.medivh.support.TypeTag;

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

    public final static byte OBJECT = 0;
    public final static byte STRING = 1;
    public final static byte ARRAY = 2;
    public final static byte COLLECTION = 3;


    public static final char UNKNOWN = '-';

    private static final char BYTE = 'B';
    private static final char SHORT = 'S';
    private static final char INT = 'I';
    private static final char LONG = 'L';
    private static final char FLOAT = 'F';
    private static final char DOUBLE = 'D';

    static final char BOOLEAN = 'b';
    static final char CHAR = 'c';

    private static final Map<Character, Number> MIN = new HashMap<Character, Number>();
    private static final Map<Character, Number> MAX = new HashMap<Character, Number>();

    static {
        MIN.put(BYTE, Byte.MIN_VALUE);
        MAX.put(BYTE, Byte.MAX_VALUE);

        MIN.put(SHORT, Short.MIN_VALUE);
        MAX.put(SHORT, Short.MAX_VALUE);

        MIN.put(INT, Integer.MIN_VALUE);
        MAX.put(INT, Integer.MAX_VALUE);

        MIN.put(LONG, Long.MIN_VALUE);
        MAX.put(LONG, Long.MAX_VALUE);

        MIN.put(FLOAT, -Float.MAX_VALUE);
        MAX.put(FLOAT, Float.MAX_VALUE);

        MIN.put(DOUBLE, -Double.MAX_VALUE);
        MAX.put(DOUBLE, Double.MAX_VALUE);
    }

    public static boolean isDecimal(char type) {
        return type == TypeUtil.FLOAT || type == TypeUtil.DOUBLE;
    }

    /**
     * 检查是否原生类型
     * @param name 类名
     * @return 是则返回 true
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
     * 获取原生类型
     * @param name 类名
     * @return 类型标记
     */
    public static Character getPrimitiveType(String name) {
        if (name.equals(getName(byte.class))) {
            return BYTE;
        }
        if (name.equals(getName(short.class))) {
            return SHORT;
        }
        if (name.equals(getName(int.class))) {
            return INT;
        }
        if (name.equals(getName(long.class))) {
            return LONG;
        }
        if (name.equals(getName(float.class))) {
            return FLOAT;
        }
        if (name.equals(getName(double.class))) {
            return DOUBLE;
        }
        if (name.equals(getName(boolean.class))) {
            return BOOLEAN;
        }
        if (name.equals(getName(char.class))) {
            return CHAR;
        }
        return null;
    }

    /**
     * 获取包装类型
     * @param name 类名称
     * @return 是原生类型则返回包装类名，否则返回愿类型
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

    private static String getName(Class<?> clazz) {
        return clazz.getName();
    }

    public static boolean isStr(String name) {
        return name.equals(getName(String.class));
    }

    public static boolean isArr(String name) {
        return name.equals("Array") || name.endsWith("[]");
    }

    /**
     * 返回数字类型标记
     * @param name 类名
     * @return 符合映射则返回对应标记，否则返回未知标记
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
     * @param name 类名
     * @return 符合映射则返回对应标签，否则返回 null
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
     * @param type 类型
     * @return 符合映射则返回对应标签，否则返回 null
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

            case CHAR:
                typeTag = TypeTag.CHAR;
                break;
            case BOOLEAN:
                typeTag = TypeTag.BOOLEAN;
                break;
            default:
                typeTag = null;
        }

        return typeTag;
    }

    /**
     * 获取最小数
     * @param type 类型
     * @param input 输入数据
     * @return 获取返回数值，输入数据有误则返回 null
     */
    public static Number getMin(char type, String input) {
        Number value = getNumberValue(type, input);
        return getMin(type, value);
    }

    /**
     * 获取最大数
     * @param type 类型
     * @param input 输入数据
     * @return 获取返回数值，输入数据有误则返回 null
     */
    public static Object getMax(char type, String input) {
        Number value = getNumberValue(type, input);
        return getMax(type, value);
    }

    /**
     * 获取最小数
     * @param type 类型
     * @param value 数值
     * @return 获取返回数值，输入数据有误则返回 null
     */
    public static Number getMin(char type, Number value) {
        if (null == value) {
            return null;
        }

        Number min = MIN.get(type);
        int result = compare(type, value, min);
        if (result == 1) {
            return getValue(type, value);
        }
        return null;
    }

    /**
     * 获取最大数
     * @param type 类型
     * @param value 数值
     * @return 获取返回数值，输入数据有误则返回 null
     */
    public static Number getMax(char type, Number value) {
        if (null == value) {
            return null;
        }

        Number max = MAX.get(type);
        int result = compare(type, value, max);
        if (result == -1) {
            return getValue(type, value);
        }
        return null;
    }

    /**
     * 转换数值
     * @param type 类型
     * @param input 输入
     * @return 当输入有误则返回 null，则否返回解析数值
     */
    static Number getNumberValue(char type, String input) {
        if (null == input) {
            return null;
        }

        Number value;
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
     * 映射对应类型数值
     * @param type 类型
     * @param value 源数值
     * @return 数值
     */
    public static Number getValue(char type, Number value) {
        switch (type) {
            case BYTE:
                value = value.byteValue();
                break;
            case SHORT:
                value = value.shortValue();
                break;
            case INT:
                value = value.intValue();
                break;
            case LONG:
                value = value.longValue();
                break;
            case FLOAT:
                value = value.floatValue();
                break;
            case DOUBLE:
                value = value.doubleValue();
                break;
        }
        return value;
    }

    /**
     * 比较转换数值
     * @param type 类型
     * @param m1 数据1
     * @param m2 数据2
     * @return 比较结果，0 为相等，-1 为 1比2小，1 为 1比2大
     */
    public static int compare(char type, Number m1, Number m2) {
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
                return Float.compare(m1.floatValue(), m2.floatValue());
            case DOUBLE:
                return Double.compare(m1.doubleValue(), m2.doubleValue());
        }
        return 0;
    }
}
