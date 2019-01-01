package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 参数校验
 * @author xueyikang
 * @since 1.0
 * @deprecated use {@link Throw} or {@link Return}
 **/
@Deprecated
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Check {

    /**
     * 方法参数是否允许为空
     */
    boolean nullable() default false;

    /**
     * 校验失败时返回类型
     */
    boolean throwable() default true;

    /**
     * throwable() == true 或 失败返回类型为 void 时抛出异常
     */
    Class<? extends RuntimeException> invalid() default IllegalArgumentException.class;

    /**
     * 校验失败时返回对象，当 throwable() == false 时生效
     * 基本类型直接设置值，如 "23", "true", "test"
     * 对象类型可设置返回 "null"
     * 非基本类型可使用构造函数，支持参数列表为基本类型
     */
    String[] returnValue() default {};

    /**
     * 自定义异常头信息
     */
    String message() default "";
}
