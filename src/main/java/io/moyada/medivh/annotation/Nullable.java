package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 允许对象为空
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Nullable {

}
