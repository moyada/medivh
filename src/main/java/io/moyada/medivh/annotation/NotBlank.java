package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 校验 String 对象不可为空白字符串
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface NotBlank {

}
