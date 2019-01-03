package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 空间范围属性规则，可用于 String，数组，集合，Map
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface SizeRule {

    /**
     * 最小空间大小/长度
     */
    int min() default -1;

    /**
     * 最大空间大小/长度
     */
    int max() default -1;
}
