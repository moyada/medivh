package io.moyada.medivh.util;

/**
 * 构造元素
 * @author xueyikang
 * @since 1.0
 **/
public class Element {

    // 校验方法名配置
    private static final String METHOD_KEY = "medivh.method";

    // 默认校验方法名
    private static final String DEFAULT_METHOD_NAME = "invalid0";

    // 临时变量名配置
    private static final String VARIABLE_KEY = "medivh.var";

    // 默认临时变量名
    public static final String DEFAULT_VARIABLE_NAME = "mvar_0";

    // 异常信息头配置
    private static final String MESSAGE_KEY = "medivh.message";

    // 默认异常信息头
    private static final String DEFAULT_MESSAGE = "Invalid input parameter";

    // 临时变量参数
    public final static String LOCAL_VARIABLE = SystemUtil.getProperty(VARIABLE_KEY, DEFAULT_VARIABLE_NAME);

    // 方法名称
    public final static String METHOD_NAME = SystemUtil.getProperty(METHOD_KEY, DEFAULT_METHOD_NAME);

    // 异常信息头
    public final static String MESSAGE = SystemUtil.getProperty(MESSAGE_KEY, DEFAULT_MESSAGE);
}
