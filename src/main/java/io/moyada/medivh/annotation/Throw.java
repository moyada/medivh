package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 参数校验失败时抛出异常
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Throw {

    /**
     * 异常类型，需提供字符串构造方法
     * @return 异常类
     */
    Class<? extends RuntimeException> value() default IllegalArgumentException.class;

    /**
     * 自定义异常头信息
     * @return 异常信息
     */
    String message() default "";
}
