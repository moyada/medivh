package cn.moyada.method.validator.annotation;

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

}
