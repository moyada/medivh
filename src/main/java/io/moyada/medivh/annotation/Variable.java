package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 设置编译产生的临时变量名或方法名，不指定使用缺省名称
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Variable {

    /**
     * 当使用非法名称时失效，如#, @, 123xx
     * @return 名称
     */
    String value();
}
