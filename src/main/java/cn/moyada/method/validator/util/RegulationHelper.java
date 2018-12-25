package cn.moyada.method.validator.util;

import cn.moyada.method.validator.annotation.Rule;
import cn.moyada.method.validator.regulation.BaseRegulation;
import cn.moyada.method.validator.regulation.LengthRegulation;
import cn.moyada.method.validator.regulation.NumberRegulation;
import com.sun.tools.javac.tree.JCTree;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RegulationHelper {

    private RegulationHelper() { }

    /**
     * 获取属性规则
     * @param ele
     * @return
     */
    public static BaseRegulation getRule(JCTree.JCVariableDecl ele) {
        Rule rule = ele.sym.getAnnotation(Rule.class);
        String type = CTreeUtil.getOriginalTypeName(ele.sym);

        BaseRegulation validation = null;
        // 字符串逻辑
        if (TypeUtil.isStr(type)) {
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
        if (TypeUtil.isArr(type)) {
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
        char numType = TypeUtil.getNumType(type);
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

        if (TypeUtil.isPrimitive(type)) {
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
