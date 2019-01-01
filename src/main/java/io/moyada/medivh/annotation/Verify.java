package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 开启参数校验
 * @author xueyikang
 * @since 1.0
 * @deprecated use {@link Variable}
 **/
@Deprecated
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Verify {

    /**
     * 临时对象名称
     */
    String var() default "";
}
