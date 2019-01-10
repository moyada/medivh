package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 整数最大值规则
 * 兼容浮点数使用
 * @author xueyikang
 * @since 1.3.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Max {

    /**
     * @return 整数最大值
     */
    long value();
}
