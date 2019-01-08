package io.moyada.medivh.annotation;

import java.lang.annotation.*;

/**
 * 参数校验失败时返回对象，当返回类型为 void 时无效
 * @author xueyikang
 * @since 1.0
 **/
@Documented
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Return {

    /**
     * 基本类型直接设置值，如 "23", "true", "test"
     * 对象类型可设置返回 "null"
     * 非基本类型可使用构造函数，支持参数列表为基本类型
     * 默认返回空构造方法对象
     * @return 返回值的构造数据
     */
    String[] value() default {};

    /**
     * 指定返回类型，需为返回类型或子类或实现类，基础类型无需设置
     * @return 返回类型
     */
    Class type() default Object.class;

    /**
     * 使用静态方法创建返回数据，需要在可访问范围内
     * @return 方法名
     */
    String staticMethod() default "";
}
