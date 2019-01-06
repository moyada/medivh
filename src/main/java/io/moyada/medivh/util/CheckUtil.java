package io.moyada.medivh.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import io.moyada.medivh.annotation.Exclusive;
import io.moyada.medivh.annotation.Return;
import io.moyada.medivh.annotation.Throw;
import io.moyada.medivh.annotation.Variable;
import io.moyada.medivh.core.Element;

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
     * @param className
     * @param methodName
     */
    public static void addCheckMethod(String className, String methodName) {
        methodNameMap.put(className, methodName);
    }

    /**
     * 是否自定规则类
     * @param className
     * @return
     */
    public static boolean isRegulable(String className) {
        return methodNameMap.containsKey(className);
    }

    /**
     * 获取定义规则类所创建的校验方法名
     * @param className
     * @return
     */
    public static String getCheckMethod(String className) {
        String methodName = methodNameMap.get(className);
        if (null == methodName) {
            throw new NullPointerException("cannot find " + className + " invalid method.");
        }
        return methodName;
    }

    /**
     * 是否存在校验
     * @param classDecl
     * @return
     */
    public static boolean isCheckClass(JCTree.JCClassDecl classDecl) {
        if (null == classDecl) {
            return false;
        }
        return isCheckSymbol(classDecl.sym);
    }

    public static boolean isCheckMethod(JCTree.JCMethodDecl methodDecl) {
        if (null == methodDecl) {
            return false;
        }
        return isCheckSymbol(methodDecl.sym);
    }

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
     * 是否排除校验
     * @param symbol
     * @return
     */
    public static boolean isExclusive(Symbol symbol) {
        return null != CTreeUtil.getAnnotation(symbol, Exclusive.class);
    }

    /**
     * 获取临时变量名
     * @param symbol
     * @return
     */
    public static String getTmpVar(Symbol symbol) {
        Variable variable = CTreeUtil.getAnnotation(symbol, Variable.class);
        return getValue(variable, Element.LOCAL_VARIABLE);
    }

    /**
     * 获取校验方法名
     * @param symbol
     * @return
     */
    public static String getTmpMethod(Symbol symbol) {
        Variable variable = CTreeUtil.getAnnotation(symbol, Variable.class);
        return getValue(variable, Element.METHOD_NAME);
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
