package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 属性规则
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface NumberRule {

    /**
     * 数字类型的最小值
     */
    String min() default "";

    /**
     * 数字类型的最大值
     */
    String max() default "";
}
