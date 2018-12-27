package io.moyada.medivh.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型工具
 * @author xueyikang
 * @since 1.0
 **/
public final class TypeUtil {

    public static final char UNKNOWN = '-';

    private static final char BYTE = 'B';
    private static final char SHORT = 'S';
    private static final char INTEGER = 'I';
    private static final char LONG = 'L';
    private static final char FLOAT = 'F';
    private static final char DOUBLE = 'D';

    private static final Map<Character, Long> MIN = new HashMap<Character, Long>();
    private static final Map<Character, Long> MAX = new HashMap<Character, Long>();

    static {
        MIN.put(BYTE, Byte.valueOf(Byte.MIN_VALUE).longValue());
        MAX.put(BYTE, Byte.valueOf(Byte.MAX_VALUE).longValue());

        MIN.put(SHORT, Short.valueOf(Short.MIN_VALUE).longValue());
        MAX.put(SHORT, Short.valueOf(Short.MAX_VALUE).longValue());

        MIN.put(INTEGER, Integer.valueOf(Integer.MIN_VALUE).longValue());
        MAX.put(INTEGER, Integer.valueOf(Integer.MAX_VALUE).longValue());

        MIN.put(LONG, Long.MIN_VALUE);
        MAX.put(LONG, Long.MAX_VALUE);

        MIN.put(FLOAT, Float.valueOf(Float.MIN_VALUE).longValue());
        MAX.put(FLOAT, Float.valueOf(Float.MAX_VALUE).longValue());

        MIN.put(DOUBLE, Double.valueOf(Double.MIN_VALUE).longValue());
        MAX.put(DOUBLE, Double.valueOf(Double.MAX_VALUE).longValue());
    }

    /**
     * 获取数字类型最小值
     * @param type
     * @return
     */
    public static long getMin(char type) {
        return MIN.get(type);
    }

    /**
     * 获取数字类型最大值
     * @param type
     * @return
     */
    public static long getMax(char type) {
        return MAX.get(type);
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
        return name.endsWith("[]");
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
}
