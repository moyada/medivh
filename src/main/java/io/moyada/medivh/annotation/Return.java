package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 参数校验失败时返回对象
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Return {

    /**
     * 基本类型直接设置值，如 "23", "true", "test"
     * 对象类型可设置返回 "null"
     * 非基本类型可使用构造函数，支持参数列表为基本类型
     */
    String[] value() default {};

    Class type() default Object.class;
}
