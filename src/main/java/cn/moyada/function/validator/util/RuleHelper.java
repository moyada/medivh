package cn.moyada.function.validator.util;

import cn.moyada.function.validator.annotation.Rule;
import cn.moyada.function.validator.core.BaseValidation;
import cn.moyada.function.validator.core.NumberValidation;
import cn.moyada.function.validator.core.StringValidation;
import com.sun.tools.javac.tree.JCTree;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RuleHelper {

    private static final char UNKNOWN = '-';

    private static final char BYTE = 'B';
    private static final char SHORT = 'S';
    private static final char INTEGER = 'I';
    private static final char LONG = 'L';
    private static final char FLOAT = 'F';
    private static final char DOUBLE = 'D';

    private static final Map<Character, Long> MIN = new HashMap<>();
    private static final Map<Character, Long> MAX = new HashMap<>();

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

    private RuleHelper() { }

    /**
     * 获取属性规则
     * @param ele
     * @return
     */
    public static BaseValidation getRule(JCTree.JCVariableDecl ele) {
        Rule rule = ele.sym.getAnnotation(Rule.class);
        String type = CTreeUtil.getOriginalTypeName(ele.sym);

        BaseValidation validation = null;
        // 字符串逻辑
        if (isStr(type)) {
            int length = rule.maxLength();
            if (length < 1) {
                validation = new BaseValidation();
            } else {
                validation = new StringValidation(length);
            }

            validation.setNullable(rule.nullable());
            return validation;
        }

        // 数字逻辑
        char numType = getNumType(type);
        if (numType != UNKNOWN) {
            long min = getMin(numType, rule.min());
            long max = getMax(numType, rule.max());

            if (needCheck(numType, min, max)) {
                validation = new NumberValidation(min, max);
            }
        }

        // 其他
        if (validation == null) {
            validation = new BaseValidation();
        }

        if (isPrimitive(type)) {
            validation.setPrimitive(true);
            validation.setNullable(true);
        } else {
            validation.setNullable(rule.nullable());
        }

        if (isEmpty(validation)) {
            return null;
        }
        return validation;
    }

    public static boolean isPrimitive(String name) {
        switch (name) {
            case "byte" :
            case "short" :
            case "int" :
            case "long" :
            case "char" :
            case "float" :
            case "double" :
            case "boolean" :
                return true;
            default:
                return false;
        }
    }

    private static boolean isStr(String name) {
        return name.equals("java.lang.String");
    }

    private static char getNumType(String name) {
        switch (name) {
            case "byte" :
            case "java.lang.Byte" :
                return BYTE;
            case "short" :
            case "java.lang.Short" :
                return SHORT;
            case "int" :
            case "java.lang.Integer" :
                return INTEGER;
            case "long" :
            case "java.lang.Long" :
                return LONG;
            case "float" :
            case "java.lang.Float" :
                return FLOAT;
            case "double" :
            case "java.lang.Double" :
                return DOUBLE;
            default:
                return UNKNOWN;
        }
    }

    private static long getMin(char type, long value) {
        Long min = MIN.get(type);
        if (min < value) {
            return value;
        }
        return min;
    }

    private static long getMax(char type, long value) {
        Long max = MAX.get(type);
        if (value < max) {
            return value;
        }
        return max;
    }

    /**
     * 范围最大无需校验
     * @param type
     * @param min
     * @param max
     * @return
     */
    private static boolean needCheck(char type, long min, long max) {
        return MIN.get(type) != min || MAX.get(type) != max;
    }

    /**
     * 无效校验规则
     * @param validation
     * @return
     */
    private static boolean isEmpty(BaseValidation validation) {
        return validation.isNullable() && validation.getClass().equals(BaseValidation.class);
    }
}
