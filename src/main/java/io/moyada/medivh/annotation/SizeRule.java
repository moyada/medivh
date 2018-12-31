package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 属性规则
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SizeRule {

    /**
     * 字符串或数组的最小长度
     */
    int min() default -1;

    /**
     * 字符串或数组的最大长度
     */
    int max() default -1;
}
