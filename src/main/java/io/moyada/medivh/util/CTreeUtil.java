package io.moyada.medivh.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

import java.lang.reflect.Method;

/**
 * 语法树工具
 * @author xueyikang
 * @since 1.0
 **/
public final class CTreeUtil {

    public static List<JCTree.JCExpression> emptyParam(){
        return List.nil();
    }

    public static ListBuffer<JCTree.JCStatement> newStatement(){
        return new ListBuffer<JCTree.JCStatement>();
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
