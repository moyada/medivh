package io.moyada.medivh.annotation;

import io.moyada.medivh.regulation.LengthRegulation;

import java.lang.annotation.*;

/**
 * 属性规则
 * @author xueyikang
 * @since 1.0
 **/
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Rule {

    /**
     * 是否允许为空，基础类型无效
     */
    boolean nullable() default false;

    /**
     * 数字类型的最小值
     */
    long min() default Long.MIN_VALUE;

    /**
     * 数字类型的最大值
     */
    long max() default Long.MAX_VALUE;

    /**
     * 字符串或数组的最大长度
     */
    int maxLength() default LengthRegulation.EXCLUDE;
}
