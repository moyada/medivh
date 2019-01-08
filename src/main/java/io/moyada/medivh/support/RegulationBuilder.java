package io.moyada.medivh.support;

import com.sun.tools.javac.code.Symbol;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.regulation.*;
import io.moyada.medivh.util.CTreeUtil;
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

    /**
     * 获取非空校验处理方式
     * @param symbol 元素
     * @param classType 类型
     * @return null 为不需，true 为进行非空校验，false 需要保证其他操作不抛出 NPE
     */
    public static Boolean checkNotNull(Symbol symbol, byte classType) {
        // 基础类型排除空校验
        if (classType == TypeUtil.PRIMITIVE) {
            return null;
        }

        NotNull notnull = CTreeUtil.getAnnotation(symbol, NotNull.class);
        Nullable nullable = CTreeUtil.getAnnotation(symbol, Nullable.class);

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

    /**
     * 获取校验方法规则链
     * @param symbol 元素
     * @param className 类名
     * @param type 类型
     * @return 执行规则链
     */
    public static List<Regulation> findBasicRule(Symbol symbol, String className, byte type) {
        return findBasicRule(symbol, className, type, null);
    }

    /**
     * 获取校验方法规则链
     * @param symbol 元素
     * @param className 类名
     * @param type 类型
     * @param actionData 执行数据，当接收处理语句为空时由此构造
     * @return 执行规则链
     */
    public static List<Regulation> findBasicRule(Symbol symbol, String className, byte type, ActionData actionData) {
        List<Regulation> regulations = new ArrayList<Regulation>();

        if (needNotBlank(symbol, className)) {
            NotBlankRegulation notBlankRegulation = new NotBlankRegulation();
            notBlankRegulation.setActionData(actionData);
            regulations.add(notBlankRegulation);
        }

        BaseRegulation numRegulation = buildNumber(symbol, className);
        if (null != numRegulation) {
            numRegulation.setActionData(actionData);
            regulations.add(numRegulation);
        }

        BaseRegulation sizeRegulation = buildSize(symbol, type);
        if (null != sizeRegulation) {
            sizeRegulation.setActionData(actionData);
            regulations.add(sizeRegulation);
        }

        return regulations;
    }

    /**
     * 是否需要非空字符串校验
     * @param symbol 元素
     * @param className 类名
     * @return 布尔值
     */
    private static boolean needNotBlank(Symbol symbol, String className) {
        return null != CTreeUtil.getAnnotation(symbol, NotBlank.class) && TypeUtil.isStr(className);
    }
    
    /**
     * 构建数字处理规则
     * 获取数值规则信息创建
     * 当最小值与最大值相同时返回 {@link EqualsRegulation}，最小值大于最大值返回 null，否则返回 {@link NumberRegulation}
     * @param symbol 元素
     * @param className 类名
     * @return 基础处理规则
     */
    private static BaseRegulation buildNumber(Symbol symbol, String className) {
        NumberRule numberRule = CTreeUtil.getAnnotation(symbol, NumberRule.class);
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
                    regulation = new EqualsRegulation(TypeUtil.OBJECT, TypeUtil.getTypeTag(numType), min, false);
                }
            }
        }
        return regulation;
    }

    /**
     * 获取空间处理规则
     * 获得空间规则数据创建
     * 当类型不属于 String 或 Collection, Map 的类型或实现类将返回 null
     * @param symbol 元素
     * @param type 类型
     * @return 基础处理规则
     */
    public static BaseRegulation buildSize(Symbol symbol, byte type) {
        SizeRule sizeRule = CTreeUtil.getAnnotation(symbol, SizeRule.class);
        if (null == sizeRule) {
            return null;
        }
        if (type < TypeUtil.STRING || type > TypeUtil.COLLECTION) {
            return null;
        }

        Integer minLength = getLength(sizeRule.min());
        Integer maxLength = getLength(sizeRule.max());

        if (!isInvalid(minLength, maxLength)) {
            if (isEquals(minLength, maxLength)) {
                return new EqualsRegulation(type, TypeTag.INT, minLength, false);
            }
            return new SizeRangeRegulation(minLength, maxLength, type);
        }

        return null;
    }

    /**
     * 数值范围是否无效
     * @param min 最大值
     * @param max 最小值
     * @return 无效则返回 true
     */
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

    /**
     * 获取长度信息，小于 0 则返回 null
     * @param length 长度值
     * @return 长度对象
     */
    private static Integer getLength(int length) {
        return length < 0 ? null : length;
    }

    /**
     * 数值是否相等
     * @param min 最小值
     * @param max 最大值
     * @return 非空且相等则返回 true
     */
    private static boolean isEquals(Integer min, Integer max) {
        return null != min && null != max && min.intValue() == max.intValue();
    }
}
