package cn.moyada.function.validator.annotation;

import cn.moyada.function.validator.validation.StringValidation;

import java.lang.annotation.*;

/**
 * @author xueyikang
 * @since 1.0
 **/
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Rule {

    boolean nullable() default false;

    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    int maxLength() default StringValidation.EXCLUDE;
}
