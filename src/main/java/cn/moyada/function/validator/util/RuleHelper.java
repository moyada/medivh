package cn.moyada.function.validator.util;

import cn.moyada.function.validator.annotation.Rule;
import cn.moyada.function.validator.core.BaseValidation;
import cn.moyada.function.validator.core.NumberValidation;
import cn.moyada.function.validator.core.StringValidation;
import com.sun.tools.javac.tree.JCTree;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RuleHelper {

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
        if (TypeUtil.isStr(type)) {
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
        char numType = TypeUtil.getNumType(type);
        if (numType != TypeUtil.UNKNOWN) {
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

        if (TypeUtil.isPrimitive(type)) {
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
    private static boolean isEmpty(BaseValidation validation) {
        return validation.isNullable() && validation.getClass().equals(BaseValidation.class);
    }
}
