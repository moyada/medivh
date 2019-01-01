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

    public static List<JCTree.JCExpression> emptyParam(){
        return List.nil();
    }

    public static ListBuffer<JCTree.JCStatement> newStatement(){
        return new ListBuffer<JCTree.JCStatement>();
    }

    /**
     * 获取注解类型属性
     * @param ms
     * @param className
     * @return
     */
    public static Attribute.Compound getAnnotationAttr(List<Attribute.Compound> ms, String className) {
        for (Attribute.Compound m : ms) {
            if (!m.getAnnotationType().toString().equals(className)) {
                continue;
            }

            return m;
        }
        return null;
    }

    /**
     * 获取注解参数
     * @param annotationAttr
     * @param key
     * @return
     */
    public static String getAnnotationValue(Attribute.Compound annotationAttr, String key) {
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
     * 获取实际类型名
     * @param symbol
     * @return
     */
    public static String getOriginalTypeName(Symbol symbol) {
        return symbol.asType().asElement().toString();
    }

    /**
     * 获取返回类型名称
     * @param methodDecl
     * @return
     */
    public static String getReturnTypeName(JCTree.JCMethodDecl methodDecl) {
        String returnTypeName = getOriginalTypeName(methodDecl.sym.getReturnType().asElement());
        if (returnTypeName.equals("void") || returnTypeName.equals("java.lang.Void")) {
            return null;
        }

        return returnTypeName;
    }

    /**
     * 是否是抽象或接口
     * @param flags
     * @return
     */
    public static boolean isAbsOrInter(long flags) {
        return ((flags & Flags.INTERFACE) != 0 || (flags & Flags.ABSTRACT) != 0);
    }

    /**
     * 获取类型节点构造值
     * @param baseType
     * @param value
     * @return
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
     * @param instance
     * @return
     */
    public static Object newInstanceForName(Context instance) {
        Method method;
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
                Class<?> tableClass = ClassUtil.getClass("com.sun.tools.javac.util.Name$Table");
                method = ClassUtil.getMethod(tableClass, "instance", Context.class);
                break;
            default:
                Class<?> namesClass = ClassUtil.getClass("com.sun.tools.javac.util.Names");
                method = ClassUtil.getMethod(namesClass, "instance", Context.class);
                break;
        }
        return ClassUtil.invoke(method, null, instance);
    }

    /**
     * 获取生成器创建 Name
     * @param instance
     * @param name
     * @return
     */
    public static Name getName(Object instance, String name) {
        Method method;
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
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
     * 获取声明
     * @param isInterface
     * @return
     */
    public static long getNewMethodFlag(boolean isInterface) {
        if (!isInterface) {
            return Flags.PUBLIC;
        }
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
            case ClassUtil.VERSION_7:
                return Flags.PUBLIC;
            default:
                Object field = ClassUtil.getStaticField(Flags.class, "DEFAULT");
                return Long.class.cast(field);
        }
    }

    /**
     * 是否可用默认方法
     * @return
     */
    public static boolean isDefaultInterface() {
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
            case ClassUtil.VERSION_7:
                return false;
            default:
                return true;
        }
    }

    /**
     * 获取类型对象
     * @param treeMaker
     * @param typeTag
     * @param value
     * @return
     */
    public static JCTree.JCLiteral newElement(TreeMaker treeMaker, TypeTag typeTag, Object value) {
        String target;
        Method method;
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
            case ClassUtil.VERSION_7:
                method = ClassUtil.getMethod(TreeMaker.class, "Literal", int.class, Object.class);
                target = "com.sun.tools.javac.code.TypeTags";
                break;
            default:
                Class<?> param = ClassUtil.getClass("com.sun.tools.javac.code.TypeTag");
                method = ClassUtil.getMethod(TreeMaker.class, "Literal", param, Object.class);
                target = "com.sun.tools.javac.code.TypeTag";
                break;
        }
        Class<?> targetClass = ClassUtil.getClass(target);
        Object field = ClassUtil.getStaticField(targetClass, typeTag.name());
        return ClassUtil.invoke(method, treeMaker, field, value);
    }

    /**
     * 获取 Binary 表达式
     * @param treeMaker
     * @param typeTag
     * @param left
     * @param right
     * @return
     */
    public static JCTree.JCExpression newExpression(TreeMaker treeMaker, TypeTag typeTag,
                                                    JCTree.JCExpression left, JCTree.JCExpression right) {
        String target;
        Method method;
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
            case ClassUtil.VERSION_7:
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
     * @param treeMaker
     * @param exceptionType
     * @return
     */
    public static JCTree.JCStatement newThrow(TreeMaker treeMaker, Object exceptionType) {
        Class<?> param;
        switch (ClassUtil.VERSION) {
            case ClassUtil.VERSION_6:
            case ClassUtil.VERSION_7:
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
