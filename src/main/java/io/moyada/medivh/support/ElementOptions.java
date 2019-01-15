package io.moyada.medivh.support;

import io.moyada.medivh.util.CheckUtil;
import io.moyada.medivh.util.SystemUtil;

/**
 * 构造元素
 * @author xueyikang
 * @since 1.0
 **/
public class ElementOptions {

    private ElementOptions() {
    }

    // 校验方法名配置
    private static final String METHOD_KEY = "medivh.method";
    private static final String DEFAULT_METHOD_NAME = "invalid0";
    public final static String METHOD_NAME = SystemUtil.getProperty(METHOD_KEY, DEFAULT_METHOD_NAME);


    // 临时变量名配置
    private static final String VARIABLE_KEY = "medivh.var";
    private static final String DEFAULT_VARIABLE_NAME = "mvar_0";
    public final static String LOCAL_VARIABLE = SystemUtil.getProperty(VARIABLE_KEY, DEFAULT_VARIABLE_NAME);


    // 异常信息头配置
    private static final String MESSAGE_KEY = "medivh.message";
    private static final String DEFAULT_MESSAGE = "Invalid input parameter";
    public final static String MESSAGE = SystemUtil.getProperty(MESSAGE_KEY, DEFAULT_MESSAGE);

    public static final String ACTION_INFO = ", cause ";

    // 非空信息配置
    private static final String NULL_KEY = "medivh.info.null";
    private static final String DEFAULT_NULL_INFO = "is null";
    public final static String NULL_INFO = SystemUtil.getProperty(NULL_KEY, DEFAULT_NULL_INFO);

    // 相等信息配置
    private static final String EQUALS_KEY = "medivh.info.equals";
    private static final String DEFAULT_EQUALS_INFO = "cannot equals";
    public final static String EQUALS_INFO = SystemUtil.getProperty(EQUALS_KEY, DEFAULT_EQUALS_INFO);

    // 小于信息配置
    private static final String LESS_KEY = "medivh.info.less";
    private static final String DEFAULT_LESS_INFO = "less than";
    public final static String LESS_INFO = SystemUtil.getProperty(LESS_KEY, DEFAULT_LESS_INFO);

    // 大于信息配置
    private static final String GREAT_KEY = "medivh.info.great";
    private static final String DEFAULT_GREAT_INFO = "great than";
    public final static String GREAT_INFO = SystemUtil.getProperty(GREAT_KEY, DEFAULT_GREAT_INFO);

    // 大于信息配置
    private static final String BLANK_KEY = "medivh.info.blank";
    private static final String DEFAULT_BLANK_INFO = "is blank";
    public final static String BLANK_INFO = SystemUtil.getProperty(BLANK_KEY, DEFAULT_BLANK_INFO);

    // 是否创建工具类
    private static final String UTIL_CREATE_KEY = "medivh.util.create";
    public final static String UTIL_CREATE = SystemUtil.getProperty(UTIL_CREATE_KEY, "true");

    // 工具类
    public static String UTIL_CLASS;
    // 非空白字符串方法
    public static String BLANK_METHOD = "isBlank";

    // 默认原生布尔值返回
    private static final String RETURN_BOOLEAN_KEY = "medivh.return.boolean";
    public final static String RETURN_BOOLEAN = CheckUtil.checkBoolean(System.getProperty(RETURN_BOOLEAN_KEY));

    // 默认原生数字值返回
    private static final String RETURN_NUMBER_KEY = "medivh.return.number";
    public final static String RETURN_NUMBER = CheckUtil.checkNumber(System.getProperty(RETURN_NUMBER_KEY));

    // 默认原生字符值返回
    private static final String RETURN_CHAR_KEY = "medivh.return.char";
    public final static String RETURN_CHAR = CheckUtil.checkChar(System.getProperty(RETURN_CHAR_KEY));
}
