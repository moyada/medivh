package cn.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 参数校验
 * @author xueyikang
 * @since 1.0
 **/
@Inherited
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Check {

    /**
     * 规则校验失败时抛出异常
     */
    Class<? extends RuntimeException> invalid() default IllegalArgumentException.class;

    /**
     * 异常头信息
     */
    String message() default "";

    /**
     * 方法参数是否允许为空
     */
    boolean nullable() default false;
}
