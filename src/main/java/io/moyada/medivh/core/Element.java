package io.moyada.medivh.core;

import io.moyada.medivh.annotation.Variable;
import io.moyada.medivh.util.StringUtil;
import io.moyada.medivh.util.SystemUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 构造元素
 * @author xueyikang
 * @since 1.0
 **/
public class Element {

    private Element() {
    }

    // 校验方法名配置
    private static final String METHOD_KEY = "medivh.method";
    private static final String DEFAULT_METHOD_NAME = "invalid0";
    public final static String METHOD_NAME = SystemUtil.getProperty(METHOD_KEY, DEFAULT_METHOD_NAME);


    // 临时变量名配置
    private static final String VARIABLE_KEY = "medivh.var";
    private static final String DEFAULT_VARIABLE_NAME = "mvar_0";
    private final static String LOCAL_VARIABLE = SystemUtil.getProperty(VARIABLE_KEY, DEFAULT_VARIABLE_NAME);


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

    // 非空白字符串方法
    private static final String BLANK_METHOD_KEY = "medivh.method.blank";
    private static final String[] DEFAULT_BLANK_METHOD = new String[] {StringUtil.class.getName(), "isBlank"};
    public final static String[] BLANK_METHOD = SystemUtil.getClassAndMethod(
            SystemUtil.getProperty(BLANK_METHOD_KEY, null), DEFAULT_BLANK_METHOD);

    private static final Map<String, String> methodNameMap = new HashMap<String, String>();

    public static void putName(String className, String methodName) {
        methodNameMap.put(className, methodName);
    }

    public static String getName(String className) {
        String methodName = methodNameMap.get(className);
        if (null == methodName) {
            throw new NullPointerException("cannot find " + className + " invalid method.");
        }
        System.out.println(className + " -> " + methodName);
        return methodName;
    }

    /**
     * 获取临时变量名
     * @param verify
     * @return
     */
    public static String getTmpVar(Variable verify) {
        return getValue(verify, LOCAL_VARIABLE);
    }

    /**
     * 获取校验方法名
     * @param verify
     * @return
     */
    public static String getTmpMethod(Variable verify) {
        return getValue(verify, METHOD_NAME);
    }

    /**
     * 获取变量值，无效则返回默认值
     * @param verify
     * @param defaultValue
     * @return
     */
    private static String getValue(Variable verify, String defaultValue) {
        if (null == verify) {
            return defaultValue;
        }
        String var = verify.value();
        if (var.isEmpty()) {
            return defaultValue;
        }
        var = var.trim();
        if (var.isEmpty()) {
            return defaultValue;
        }
        var = StringUtil.fixName(var);
        if (null == var) {
            var = defaultValue;
        }
        return var;
    }

    /**
     * 是否标记返回空值
     * @param values
     * @return
     */
    public final static boolean isReturnNull(String[] values) {
        if (null == values) {
            return true;
        }
        if (values.length == 0) {
            return true;
        }
        if (values.length != 1) {
            return false;
        }
        if (values[0].equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }
}
