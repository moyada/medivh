package io.moyada.medivh.core;

import com.sun.tools.javac.code.Symbol;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.regulation.*;
import io.moyada.medivh.util.TypeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则工具
 * @author xueyikang
 * @since 1.0
 **/
public final class RegulationBuilder {

    private RegulationBuilder() { }

    public static Boolean checkNullOrNotNull(Symbol symbol, byte classType) {
        // 基础类型排除空校验
        if (classType == TypeUtil.PRIMITIVE) {
            return null;
        }

        Nullable nullable = symbol.getAnnotation(Nullable.class);
        NotNull notnull = symbol.getAnnotation(NotNull.class);

        boolean nullcheck;
        if (null != notnull) {
            nullcheck = true;
        } else if (null != nullable) {
            nullcheck = false;
        } else {
            nullcheck = true;
        }

        if (nullcheck) {
            return true;
        }
        return false;
    }
    
    public static List<Regulation> createInvalid(Symbol symbol, String className, byte type) {
        List<Regulation> regulations = new ArrayList<Regulation>();

        if (isNotBlank(symbol, className)) {
            regulations.add(new NotBlankRegulation());
        }

        Regulation numRegulation = buildNumber(symbol, className);
        if (null != numRegulation) {
            regulations.add(numRegulation);
        }


        Regulation sizeRegulation = buildSize(symbol, type);
        if (null != sizeRegulation) {
            regulations.add(sizeRegulation);
        }

        return regulations;
    }


    private static boolean isNotBlank(Symbol symbol, String className) {
        return null != symbol.getAnnotation(NotBlank.class) && TypeUtil.isStr(className);
    }
    
    /**
     * 构建数字规则
     * @param symbol
     * @param className
     * @return
     */
    private static Regulation buildNumber(Symbol symbol, String className) {
        NumberRule numberRule = symbol.getAnnotation(NumberRule.class);
        if (null == numberRule) {
            return null;
        }

        Regulation regulation = null;
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
                    regulation = new EqualsRegulation(TypeUtil.getTypeTag(numType), min, false);
                }
            }
        }
        return regulation;
    }

    /**
     * 构建大小规则
     * @param symbol
     * @param type
     * @return
     */
    public static Regulation buildSize(Symbol symbol, byte type) {
        SizeRule sizeRule = symbol.getAnnotation(SizeRule.class);
        if (null == sizeRule) {
            return null;
        }
        if (type == TypeUtil.PRIMITIVE) {
            return null;
        }

        return getSizeRule(sizeRule, type);
    }

    private static Regulation getSizeRule(SizeRule rule, byte type) {
        Integer minLength = getLength(rule.min());
        Integer maxLength = getLength(rule.max());

        if (!isInvalid(minLength, maxLength)) {
            return new SizeRangeRegulation(minLength, maxLength, type);
        }

        return null;
    }

    private static Integer getLength(int length) {
        return length < 0 ? null : length;
    }

    private static boolean isInvalid(Integer min, Integer max) {
        if (null == min && null == max) {
            return true;
        }
        if (null != min && null != max &&
                TypeUtil.compare(TypeUtil.getNumType(int.class.getName()), min, max) > 0) {
            return true;
        }

        return false;
    }
}
