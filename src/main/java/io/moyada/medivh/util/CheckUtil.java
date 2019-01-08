package io.moyada.medivh.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import io.moyada.medivh.annotation.Exclusive;
import io.moyada.medivh.annotation.Return;
import io.moyada.medivh.annotation.Throw;
import io.moyada.medivh.annotation.Variable;
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
        return null != CTreeUtil.getAnnotation(symbol, Exclusive.class);
    }

    /**
     * 获取临时变量名
     * @param symbol 元素
     * @return 名称
     */
    public static String getTmpVar(Symbol symbol) {
        Variable variable = CTreeUtil.getAnnotation(symbol, Variable.class);
        return getValue(variable, ElementOptions.LOCAL_VARIABLE);
    }

    /**
     * 获取校验方法名
     * @param symbol 元素
     * @return 名称
     */
    public static String getTmpMethod(Symbol symbol) {
        Variable variable = CTreeUtil.getAnnotation(symbol, Variable.class);
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
}
