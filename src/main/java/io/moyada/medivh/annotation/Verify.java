package io.moyada.medivh.annotation;

import io.moyada.medivh.util.Element;

import java.lang.annotation.*;

/**
 * 开启参数校验
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Verify {

    /**
     * 临时对象名称
     */
    String value() default Element.DEFAULT_VARIABLE_NAME;
}
