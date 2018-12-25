package cn.moyada.function.validator.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.lang.reflect.InvocationTargetException;
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
        return symbol.asType().toString();
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
            case ClassUtil.OLD_VERSION:
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
        try {
            return (JCTree.JCLiteral) method.invoke(treeMaker, field, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
            case ClassUtil.OLD_VERSION:
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
        try {
            return (JCTree.JCExpression) method.invoke(treeMaker, field, left, right);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
