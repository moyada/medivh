package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 不允许对象为空，在存在配置规则时候默认启动
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface NotNull {

}
