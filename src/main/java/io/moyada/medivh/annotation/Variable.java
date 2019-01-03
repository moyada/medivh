package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 设置临时对象名称，不指定使用缺省名称
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Variable {

    String value();
}
