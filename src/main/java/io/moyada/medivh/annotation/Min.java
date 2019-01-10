package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 整数最小值规则
 * 兼容浮点数使用
 * @author xueyikang
 * @since 1.3.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Min {

    /**
     * @return 整数最小值
     */
    long value();
}
