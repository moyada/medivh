package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 浮点数最小值规则
 * 兼容整数使用
 * @author xueyikang
 * @since 1.3.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface DecimalMin {

    /**
     * @return 浮点数最小值
     */
    double value();
}
