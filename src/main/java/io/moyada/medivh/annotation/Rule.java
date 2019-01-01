package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 属性规则
 * @author xueyikang
 * @since 1.0
 * @deprecated use {@link NumberRule} or {@link SizeRule} or {@link Nullable} or {@link NotNull}
 **/
@Deprecated
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
    String min() default "";

    /**
     * 数字类型的最大值
     */
    String max() default "";

    /**
     * 字符串或数组的最小长度
     */
    int minLength() default -1;

    /**
     * 字符串或数组的最大长度
     */
    int maxLength() default -1;
}
