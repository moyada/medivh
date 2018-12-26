package cn.moyada.medivh.regulation;

import cn.moyada.medivh.annotation.Rule;
import cn.moyada.medivh.util.TypeUtil;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RegulationHelper {

    private RegulationHelper() { }

    /**
     * 获取属性规则
     * @param className
     * @param rule
     * @return
     */
    public static BaseRegulation build(String className, Rule rule, boolean isCollection) {
        BaseRegulation validation = null;

        // 集合 Collection \ Map
        if (isCollection) {
            int length = rule.maxLength();
            if (length < 1) {
                // 无意义规则
                if (rule.nullable()) {
                    return null;
                }
                validation = new BaseRegulation();
            } else {
                validation = new LengthRegulation(length, LengthRegulation.COLLECTION);
            }

            validation.setNullable(rule.nullable());
            return validation;
        }

        // 字符串逻辑
        if (TypeUtil.isStr(className)) {
            int length = rule.maxLength();
            if (length < 1) {
                // 无意义规则
                if (rule.nullable()) {
                    return null;
                }
                validation = new BaseRegulation();
            } else {
                validation = new LengthRegulation(length, LengthRegulation.STRING);
            }

            validation.setNullable(rule.nullable());
            return validation;
        }

        // 数组规则
        if (TypeUtil.isArr(className)) {
            int length = rule.maxLength();
            if (length < 1) {
                // 无意义规则
                if (rule.nullable()) {
                    return null;
                }
                validation = new BaseRegulation();
            } else {
                validation = new LengthRegulation(length, LengthRegulation.ARRAY);
            }

            validation.setNullable(rule.nullable());
            return validation;
        }

        // 数字逻辑
        char numType = TypeUtil.getNumType(className);
        if (numType != TypeUtil.UNKNOWN) {
            long min = getMin(numType, rule.min());
            long max = getMax(numType, rule.max());

            if (needCheck(numType, min, max)) {
                validation = new NumberRegulation(min, max);
            }
        }

        // 其他
        if (validation == null) {
            validation = new BaseRegulation();
        }

        // 基础类型排除空校验
        if (TypeUtil.isPrimitive(className)) {
            validation.setPrimitive(true);
            validation.setNullable(true);
        } else {
            validation.setNullable(rule.nullable());
        }

        // 无意义规则
        if (isEmpty(validation)) {
            return null;
        }
        return validation;
    }

    private static long getMin(char type, long value) {
        long min = TypeUtil.getMin(type);
        if (min < value) {
            return value;
        }
        return min;
    }

    private static long getMax(char type, long value) {
        long max = TypeUtil.getMax(type);
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
        return TypeUtil.getMin(type) != min || TypeUtil.getMax(type) != max;
    }

    /**
     * 无效校验规则
     * @param validation
     * @return
     */
    private static boolean isEmpty(BaseRegulation validation) {
        return validation.isNullable() && validation.getClass().equals(BaseRegulation.class);
    }
}
