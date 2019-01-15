package io.moyada.medivh.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.support.ElementOptions;

import javax.lang.model.element.AnnotationMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验工具
 * @author xueyikang
 * @since 1.0
 **/
public final class CheckUtil {

    private CheckUtil() {
    }

    // 类校验方法信息, 类名 - 方法名
    private static final Map<String, String> methodNameMap = new HashMap<String, String>();

    /**
     * 存储类校验方法
     * @param className 类名
     * @param methodName 方法名
     */
    public static void addCheckMethod(String className, String methodName) {
        methodNameMap.put(className, methodName);
    }

    /**
     * 是否自定规则类
     * @param className 类名
     * @return 存在返回 true
     */
    public static boolean isRegulable(String className) {
        return methodNameMap.containsKey(className);
    }

    /**
     * 获取定义规则类所创建的校验方法名
     * @param className 类名
     * @return 方法名
     */
    public static String getCheckMethod(String className) {
        String methodName = methodNameMap.get(className);
        if (null == methodName) {
            throw new NullPointerException("cannot find " + className + " invalid method.");
        }
        return methodName;
    }

    /**
     * 是否存在校验标识
     * @param classDecl 类元素
     * @return 存在校验标识返回 true
     */
    public static boolean isCheckClass(JCTree.JCClassDecl classDecl) {
        if (null == classDecl) {
            return false;
        }
        return isCheckSymbol(classDecl.sym);
    }

    /**
     * 是否存在校验标识
     * @param methodDecl 方法元素
     * @return 存在校验标识返回 true
     */
    public static boolean isCheckMethod(JCTree.JCMethodDecl methodDecl) {
        if (null == methodDecl) {
            return false;
        }
        return isCheckSymbol(methodDecl.sym);
    }

    /**
     * 是否存在校验标识
     * @param symbol 元素
     * @@return 存在校验标识返回 true
     */
    private static boolean isCheckSymbol(Symbol symbol) {
        List<? extends AnnotationMirror> mirrors = symbol.getAnnotationMirrors();
        if (mirrors.isEmpty()) {
            return false;
        }
        int size = mirrors.size();
        for (int i = 0; i < size; i++) {
            String name = mirrors.get(i).getAnnotationType().toString();
            if (name.equals(Throw.class.getName())) {
                return true;
            } else if (name.equals(Return.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否排除校验标识
     * @param symbol 元素
     * @return 存在排除标识返回 true
     */
    public static boolean isExclusive(Symbol symbol) {
        return null != TreeUtil.getAnnotation(symbol, Exclusive.class);
    }

    /**
     * 获取临时变量名
     * @param symbol 元素
     * @return 名称
     */
    public static String getTmpVar(Symbol symbol) {
        Variable variable = TreeUtil.getAnnotation(symbol, Variable.class);
        return getValue(variable, ElementOptions.LOCAL_VARIABLE);
    }

    /**
     * 获取校验方法名
     * @param symbol 元素
     * @return 名称
     */
    public static String getTmpMethod(Symbol symbol) {
        Variable variable = TreeUtil.getAnnotation(symbol, Variable.class);
        return getValue(variable, ElementOptions.METHOD_NAME);
    }

    /**
     * 获取变量值，无效则返回默认值
     * @param variable 名称注解
     * @param defaultValue 默认名称
     * @return 名称
     */
    private static String getValue(Variable variable, String defaultValue) {
        if (null == variable) {
            return defaultValue;
        }
        String var = variable.value();
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
     * 获取最小数值
     * @param symbol 元素
     * @param numType 元素
     * @return 数值
     */
    public static Number getMinNumber(Symbol symbol, char numType) {
        Double decimal = getMinDecimal(symbol);
        Long value = getMin(symbol);
        return getNumber(numType, decimal, value);
    }

    /**
     * 获取最大数值
     * @param symbol 元素
     * @param numType 元素
     * @return 数值
     */
    public static Number getMaxNumber(Symbol symbol, char numType) {
        Double decimal = getMaxDecimal(symbol);
        Long value = getMax(symbol);
        return getNumber(numType, decimal, value);
    }

    /**
     * 根据类型获取数值
     * 当当前数字类型为浮点时优先选取 浮点数，否则整数
     * @param numType 数字类型
     * @param decimal 浮点值
     * @param value 整数值
     * @return 数值
     */
    private static Number getNumber(char numType, Double decimal, Long value) {
        Number num;
        if (TypeUtil.isDecimal(numType)) {
            if (null != decimal) {
                num = decimal;
            } else {
                num = value;
            }
        } else {
            if (null != value) {
                num = value;
            } else {
                num = decimal;
            }
        }
        return num;
    }

    private static Long getMin(Symbol symbol) {
        Min min = TreeUtil.getAnnotation(symbol, Min.class);
        if (null == min) {
            return null;
        }
        return min.value();
    }

    /**
     * 获取最大整数
     * @param symbol 元素
     * @return 数值
     */
    private static Long getMax(Symbol symbol) {
        Max max = TreeUtil.getAnnotation(symbol, Max.class);
        if (null == max) {
            return null;
        }
        return max.value();
    }

    private static Double getMinDecimal(Symbol symbol) {
        DecimalMin decimalMin = TreeUtil.getAnnotation(symbol, DecimalMin.class);
        if (null == decimalMin) {
            return null;
        }
        return decimalMin.value();
    }

    private static Double getMaxDecimal(Symbol symbol) {
        DecimalMax decimalMax = TreeUtil.getAnnotation(symbol, DecimalMax.class);
        if (null == decimalMax) {
            return null;
        }
        return decimalMax.value();
    }

    /**
     * 是否标记返回空值
     * @param values 数据
     * @return 数据有且只有一个 null 则返回 true
     */
    public final static boolean isReturnNull(String[] values) {
        if (values.length == 0) {
            return false;
        }
        if (values.length != 1) {
            return false;
        }
        if (values[0].equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }

    /**
     * 字符串是否属于 null 标识
     * @param value 输入字符
     * @return 是否是 null 属性
     */
    public final static boolean isNull(String value) {
        if (value.isEmpty()) {
            return true;
        }
        if (value.equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }

    /**
     * 获取原生类型返回
     * @param classType 类型标记
     * @return 返回数据字符串
     */
    public static String getPrimitiveReturn(char classType) {
        String r;
        switch (classType) {
            case TypeUtil.BOOLEAN:
                r = ElementOptions.RETURN_BOOLEAN;
                break;
            case TypeUtil.CHAR:
                r = ElementOptions.RETURN_CHAR;
                break;
            default:
                r = ElementOptions.RETURN_NUMBER;
        }
        return r;
    }

    /**
     * 检查字符串是否为数字类型，否则返回 null
     * @param input 输入
     * @return 规则后字符串
     */
    public static String checkNumber(String input) {
        input = StringUtil.trim(input);
        if (null == input) {
            return null;
        }
        char ch = input.charAt(0);
        if (!StringUtil.isDigital(ch) && ch != '-') {
            return null;
        }

        int length = input.length();
        boolean hasFloat = false;
        for (int i = 1; i < length; i++) {
            ch = input.charAt(i);
            if (StringUtil.isDigital(ch)) {
                continue;
            }
            if (ch == '.' && !hasFloat) {
                hasFloat = true;
                continue;
            }

            return null;
        }

        return input;
    }

    /**
     * 检查字符串是否为布尔值，否则返回 null
     * @param input 输入
     * @return 规则后字符串
     */
    public static String checkBoolean(String input) {
        input = StringUtil.trim(input);
        if (null == input) {
            return null;
        }

        if (Boolean.TRUE.toString().equalsIgnoreCase(input)) {
            return input;
        }

        if (Boolean.FALSE.toString().equalsIgnoreCase(input)) {
            return input;
        }
        return null;
    }

    /**
     * 检查输入字符串是否为字符，否则返回 null
     * @param input 输入
     * @return 规则后字符串
     */
    public static String checkChar(String input) {
        if (null == input) {
            return null;
        }
        if (input.length() != 1) {
            return null;
        }
        return Character.toString(input.charAt(0));
    }
}
