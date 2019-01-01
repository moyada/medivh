package io.moyada.medivh.regulation;

import com.sun.tools.javac.code.Symbol;
import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.Nullable;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;
import io.moyada.medivh.util.TypeUtil;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RegulationHelper {

    private RegulationHelper() { }

    /**
     * 获取属性规则
     * @param symbol
     * @param className
     * @param isCollection
     * @return
     */
    public static BaseRegulation build(Symbol symbol, String className, boolean isCollection) {
        BaseRegulation regulation;
        // 获取参数规则

        NumberRule numberRule = symbol.getAnnotation(NumberRule.class);
        regulation = buildNumber(className, numberRule);

        if (null == regulation) {
            SizeRule sizeRule = symbol.getAnnotation(SizeRule.class);
            regulation = buildSize(className, sizeRule, isCollection);
        }

        boolean nullable;

        Nullable nullflag = symbol.getAnnotation(Nullable.class);
        if (null != nullflag) {
            nullable = true;
        } else if (regulation != null){
            nullable = false;
        } else {
            NotNull notnull = symbol.getAnnotation(NotNull.class);
            nullable = notnull == null;
        }

        return check(regulation, className, nullable);
    }

    private static BaseRegulation check(BaseRegulation validation, String className, boolean nullable) {
        if (null == validation) {
            if (nullable) {
                return null;
            }
            validation = new BaseRegulation();
        }

        // 基础类型排除空校验
        if (TypeUtil.isPrimitive(className)) {
            validation.setPrimitive(true);
            validation.setNullable(true);
        } else {
            validation.setNullable(nullable);
        }

        // 无意义规则
        if (isEmpty(validation)) {
            return null;
        }
        return validation;
    }

    /**
     * 构建数字规则
     * @param className
     * @param numberRule
     * @return
     */
    private static BaseRegulation buildNumber(String className, NumberRule numberRule) {
        if (null == numberRule) {
            return null;
        }

        BaseRegulation regulation = null;

        // 数字逻辑
        char numType = TypeUtil.getNumType(className);
        if (numType != TypeUtil.UNKNOWN) {
            Object min = TypeUtil.getMin(numType, numberRule.min());
            Object max = TypeUtil.getMax(numType, numberRule.max());

            int result;
            if (null == min || null == max) {
                result = -1;
            } else {
                result = TypeUtil.compare(numType, min, max);
            }

            if (null != min || null != max) {
                if (result == -1) {
                    regulation = new NumberRegulation(TypeUtil.getTypeTag(numType), min, max);
                } else if (result == 0) {
                    regulation = new EqualsRegulation(TypeUtil.getTypeTag(numType), min);
                }
            }
        }
        return regulation;
    }

    /**
     * 构建大小规则
     * @param className
     * @param sizeRule
     * @param isCollection
     * @return
     */
    private static BaseRegulation buildSize(String className, SizeRule sizeRule, boolean isCollection) {
        if (null == sizeRule) {
            return null;
        }

        BaseRegulation regulation = null;

        // 集合 Collection \ Map
        if (isCollection) {
            return getSizeRule(sizeRule, SizeRegulation.COLLECTION);
        }

        // 字符串逻辑
        if (TypeUtil.isStr(className)) {
            return getSizeRule(sizeRule, SizeRegulation.STRING);
        }

        // 数组规则
        if (TypeUtil.isArr(className)) {
            return getSizeRule(sizeRule, SizeRegulation.ARRAY);
        }

        return regulation;
    }

    private static BaseRegulation getSizeRule(SizeRule rule, byte type) {
        Integer minLength = getLength(rule.min());
        Integer maxLength = getLength(rule.max());

        BaseRegulation validation;
        if (isInvalid(minLength, maxLength)) {
            validation = new BaseRegulation();
        } else {
            validation = new SizeRegulation(minLength, maxLength, type);
        }
        return validation;
    }

    private static Integer getLength(int length) {
        return length < 0 ? null : length;
    }

    private static boolean isInvalid(Integer min, Integer max) {
        if (null == min && null == max) {
            return true;
        }
        if (null != min && null != max &&
                TypeUtil.compare(TypeUtil.getNumType("int"), min, max) > 0) {
            return true;
        }

        return false;
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
