package io.moyada.medivh.util;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import io.moyada.medivh.support.TypeTag;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 语法树工具
 * @author xueyikang
 * @since 1.0
 **/
public final class CTreeUtil {

    private CTreeUtil() {
    }

    /**
     * 空参数
     * @return 空参数
     */
    public static List<JCTree.JCExpression> emptyParam(){
        return List.nil();
    }

    /**
     * 新建语句链
     * @return 语句链
     */
    public static ListBuffer<JCTree.JCStatement> newStatement(){
        return new ListBuffer<JCTree.JCStatement>();
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
     * 获取 Name 生成器对象
     * @param context 处理器上下文
     * @return Name 生成器
     */
    public static Object newInstanceForName(Context context) {
        Method method;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
                Class<?> tableClass = ClassUtil.getClass("com.sun.tools.javac.util.Name$Table");
                method = ClassUtil.getMethod(tableClass, "instance", Context.class);
                break;
            default:
                Class<?> namesClass = ClassUtil.getClass("com.sun.tools.javac.util.Names");
                method = ClassUtil.getMethod(namesClass, "instance", Context.class);
                break;
        }
        return ClassUtil.invoke(method, null, context);
    }

    /**
     * 获取生成器创建 Name
     * @param instance Name 生成器
     * @param name 名称
     * @return 名称元素
     */
    public static Name getName(Object instance, String name) {
        Method method;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
                Class<?> nameClass = ClassUtil.getClass("com.sun.tools.javac.util.Name");
                Class<?> tableClass = ClassUtil.getClass("com.sun.tools.javac.util.Name$Table");
                method = ClassUtil.getMethod(nameClass, "fromString", tableClass, String.class);
                return ClassUtil.invoke(method, null, instance, name);
            default:
                Class<?> namesClass = ClassUtil.getClass("com.sun.tools.javac.util.Names");
                method = ClassUtil.getMethod(namesClass, "fromString", String.class);
                return ClassUtil.invoke(method, instance, name);
        }
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
        return Long.class.cast(field);
    }

    /**
     * 是否可用默认方法
     * @return 版本在 8 以下则返回 false
     */
    public static boolean isDefaultInterface() {
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                return false;
            default:
                return true;
        }
    }

    /**
     * 获取构造类型标签
     * @param typeTag 类型标签
     * @return 构造类型标签
     */
    private static Object getTypeTag(TypeTag typeTag) {
        String target;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                target = "com.sun.tools.javac.code.TypeTags";
                break;
            default:
                target = "com.sun.tools.javac.code.TypeTag";
                break;
        }
        Class<?> targetClass = ClassUtil.getClass(target);
        return ClassUtil.getStaticField(targetClass, typeTag.name());
    }

    /**
     * 获取原生类型
     * @param treeMaker 语法树构造器
     * @param typeTag 类型标签
     * @return 原生类元素
     */
    public static JCTree.JCPrimitiveTypeTree getPrimitiveType(TreeMaker treeMaker, TypeTag typeTag) {
        Method method;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                method = ClassUtil.getMethod(TreeMaker.class, "TypeIdent", int.class);
                break;
            default:
                Class<?> param = ClassUtil.getClass("com.sun.tools.javac.code.TypeTag");
                method = ClassUtil.getMethod(TreeMaker.class, "TypeIdent", param);
                break;
        }
        return ClassUtil.invoke(method, treeMaker, getTypeTag(typeTag));
    }

    /**
     * 获取类型对象
     * @param treeMaker 语法树构造器
     * @param typeTag 类型标签
     * @param value 数据
     * @return 类型元素
     */
    public static JCTree.JCLiteral newElement(TreeMaker treeMaker, TypeTag typeTag, Object value) {
        Method method;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                method = ClassUtil.getMethod(TreeMaker.class, "Literal", int.class, Object.class);
                break;
            default:
                Class<?> param = ClassUtil.getClass("com.sun.tools.javac.code.TypeTag");
                method = ClassUtil.getMethod(TreeMaker.class, "Literal", param, Object.class);
                break;
        }
        return ClassUtil.invoke(method, treeMaker, getTypeTag(typeTag), value);
    }

    /**
     * 获取 Binary 表达式
     * @param treeMaker 语法树构造器
     * @param typeTag 类型标签
     * @param left 表达式
     * @param right 比较对象
     * @return 对比语句元素
     */
    public static JCTree.JCExpression newBinary(TreeMaker treeMaker, TypeTag typeTag,
                                                JCTree.JCExpression left, JCTree.JCExpression right) {
        String target;
        Method method;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                method = ClassUtil.getMethod(TreeMaker.class, "Binary", int.class,
                        JCTree.JCExpression.class, JCTree.JCExpression.class);
                target = "com.sun.tools.javac.tree.JCTree";
                break;
            default:
                Class<?> param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree$Tag");
                method = ClassUtil.getMethod(TreeMaker.class, "Binary", param,
                        JCTree.JCExpression.class, JCTree.JCExpression.class);
                target = "com.sun.tools.javac.tree.JCTree$Tag";
                break;
        }
        Class<?> targetClass = ClassUtil.getClass(target);
        Object field = ClassUtil.getStaticField(targetClass, typeTag.name());
        return ClassUtil.invoke(method, treeMaker, field, left, right);
    }

    /**
     * 生成抛出异常语句
     * @param treeMaker 语法树构造器
     * @param exceptionType 异常类型元素
     * @return 抛出异常语句
     */
    public static JCTree.JCStatement newThrow(TreeMaker treeMaker, Object exceptionType) {
        Class<?> param;
        switch (VersionUtil.VERSION) {
            case VersionUtil.VERSION_6:
            case VersionUtil.VERSION_7:
                param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree");
                break;
            default:
                param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree$JCExpression");
                break;
        }
        Method method = ClassUtil.getMethod(TreeMaker.class, "Throw", param);
        return ClassUtil.invoke(method, treeMaker, exceptionType);
    }
}
