package io.moyada.medivh.util;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.TypeTag;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 语法树工具
 * @author xueyikang
 * @since 1.0
 **/
public final class TreeUtil {

    private TreeUtil() {
    }

    /**
     * 空参数
     * @return 空参数
     */
    public static List<JCTree.JCExpression> emptyExpression(){
        return List.<JCTree.JCExpression>nil();
    }

    /**
     * 新建语句链
     * @return 语句链
     */
    public static ListBuffer<JCTree.JCStatement> newStatement(){
        return new ListBuffer<JCTree.JCStatement>();
    }

    /**
     * 扫描多个节点
     * @param vars 节点集合
     * @param visitor 监视器
     */
    public static void visit(List<? extends JCTree> vars, JCTree.Visitor visitor) {
        for (JCTree var : vars) {
            var.accept(visitor);
        }
    }

    /**
     * 获取注解数据
     * @param symbol 元素
     * @param className 注解类名
     * @param attrKey 属性名
     * @return 数据
     */
    public static String getAnnotationValue(Symbol symbol, String className, String attrKey) {
        Attribute.Compound annotationAttr = getAnnotationAttr(symbol.getAnnotationMirrors(), className);
        if (null == annotationAttr) {
            return null;
        }
        return getAnnotationValue(annotationAttr, attrKey);
    }

    /**
     * 获取注解类型属性
     * @param ms 元素注解镜像
     * @param className 注解类名
     * @return 对应注解镜像
     */
    private static Attribute.Compound getAnnotationAttr(List<Attribute.Compound> ms, String className) {
        for (Attribute.Compound m : ms) {
            if (!m.getAnnotationType().toString().equals(className)) {
                continue;
            }

            return m;
        }
        return null;
    }

    /**
     * 获取注解参数值
     * @param annotationAttr 注解镜像
     * @param key 属性名
     * @return 数值
     */
    private static String getAnnotationValue(Attribute.Compound annotationAttr, String key) {
        if (null == annotationAttr) {
            return null;
        }

        for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : annotationAttr.getElementValues().entrySet()) {
            if (entry.getKey().toString().equals(key)) {
                return entry.getValue().getValue().toString();
            }
        }
        return null;
    }

    /**
     * 获取注解数据
     * @param symbol 元素
     * @param anno 注解类
     * @param <T> 类型
     * @return 注解数据
     */
    @SuppressWarnings("deprecation")
    public static <T extends Annotation> T getAnnotation(Symbol symbol, Class<T> anno) {
        if (null == symbol) {
            return null;
        }
        return symbol.getAnnotation(anno);
    }

    /**
     * 获取实际类型名
     * @param symbol 元素
     * @return 类名
     */
    public static String getOriginalTypeName(Symbol symbol) {
        return symbol.asType().asElement().toString();
    }

    /**
     * 获取返回类型名称
     * @param methodDecl 方法节点
     * @return 当返回类名为 void 则返回 null
     */
    public static String getReturnTypeName(JCTree.JCMethodDecl methodDecl) {
        String returnTypeName;
        Symbol.MethodSymbol methodSymbol = methodDecl.sym;
        if (methodSymbol == null) {
            JCTree returnType = methodDecl.getReturnType();
            if (null == returnType) {
                return null;
            }
            returnTypeName = returnType.toString();
        } else {
            returnTypeName = getOriginalTypeName(methodSymbol.getReturnType().asElement());
        }

        if (returnTypeName.equals("void") || returnTypeName.equals("java.lang.Void")) {
            return null;
        }

        return returnTypeName;
    }

    /**
     * 获取完整名称
     * @param methodSymbol 方法元素
     * @return 类名 + . + 方法名
     */
    public static String getFullName(Symbol methodSymbol) {
        String className = methodSymbol.getEnclosingElement().asType().toString();
        String methodName = methodSymbol.name.toString();
        return className + "." + methodName;
    }

    /**
     * 是否是抽象或接口
     * @param flags 标记
     * @return 标记非接口或抽象类则返回 true
     */
    public static boolean isAbsOrInter(long flags) {
        return ((flags & Flags.INTERFACE) != 0 || (flags & Flags.ABSTRACT) != 0);
    }

    /**
     * 获取类型节点构造值
     * @param baseType 类型标签
     * @param value 输入数据
     * @return 元素构造值
     */
    public static Object getValue(TypeTag baseType, String value) {
        if (baseType == TypeTag.CLASS) {
            return value;
        }

        if (baseType == TypeTag.BOOLEAN) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                return 1;
            }
            if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                return 0;
            }
            return null;
        }

        if (baseType == TypeTag.CHAR) {
            if (value.length() != 1) {
                return null;
            }
            return (int) value.charAt(0);
        }

        return TypeUtil.getNumberValue(TypeUtil.getNumType(baseType.toString().toLowerCase()), value);
    }

    /**
     * 获取新建方法访问标记
     * @param isInterface 是否接口
     * @return 当非接口时返回 Public，非则返回 default
     */
    public static long getNewMethodFlag(boolean isInterface) {
        if (!isInterface) {
            return Flags.PUBLIC;
        }
        Object field = ClassUtil.getStaticField(Flags.class, "DEFAULT");
        return (Long) field;
    }

    /**
     * 是否可用默认方法
     * @return 版本在 8 以下则返回 false
     */
    public static boolean hasDefaultInterface() {
        return Compiler.CURRENT_VERSION >= Compiler.JAVA_8;
    }
}
