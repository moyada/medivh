package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 排除规则校验
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Exclusive {

}
