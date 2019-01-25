package io.moyada.medivh.support;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import io.moyada.medivh.util.*;
import io.moyada.medivh.util.Compiler;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 语法创建工具
 * @author xueyikang
 * @since 1.0
 **/
public class SyntaxTreeMaker {

    // 语法树创建器
    private final TreeMaker treeMaker;
    // 元素获取器
    private final JavacElements javacElements;
    // Name 构造器
    private final Object namesInstance;
    // 类型集合
    private final Types types;

    // null 表达式
    public final JCTree.JCLiteral nullNode;

    // int 0 表达式
    public final JCTree.JCLiteral zeroIntNode;

    // true 表达式
    public final JCTree.JCLiteral trueNode;

    // false 表达式
    public final JCTree.JCLiteral falseNode;

    // ' ' 表达式
    public final JCTree.JCLiteral emptyCh;

    // string
    public final Symbol.ClassSymbol stringSymbol;
    // collection
    public final Symbol.ClassSymbol collectionSymbol;
    // map
    public final Symbol.ClassSymbol mapSymbol;

    private SyntaxTreeMaker(Context context) {
        this.treeMaker = TreeMaker.instance(context);
        this.namesInstance = newNameInstance(context);
        this.javacElements = JavacElements.instance(context);
        this.types = Types.instance(context);

        this.nullNode = newElement(TypeTag.BOT, null);
        this.trueNode = newElement(TypeTag.BOOLEAN, 1);
        this.falseNode = newElement(TypeTag.BOOLEAN, 0);
        this.zeroIntNode = newElement(TypeTag.INT, 0);
        this.emptyCh = newElement(TypeTag.CHAR, (int) ' ');

        collectionSymbol = javacElements.getTypeElement(Collection.class.getName());
        mapSymbol = javacElements.getTypeElement(Map.class.getName());
        stringSymbol = javacElements.getTypeElement(CharSequence.class.getName());

        RegulationExecutor.init(this);
    }

    public static SyntaxTreeMaker newInstance(Context context) {
        return new SyntaxTreeMaker(context);
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Types getTypes() {
        return types;
    }
    
    /**
     * 获取 Name 生成器对象
     * @param context 处理器上下文
     * @return Name 生成器
     */
    private static Object newNameInstance(Context context) {
        Method method;
        if (Compiler.CURRENT_VERSION == Compiler.JAVA_6) {
            Class<?> tableClass = ClassUtil.getClass("com.sun.tools.javac.util.Name$Table");
            method = ClassUtil.getMethod(tableClass, "instance", Context.class);
        } else {
            Class<?> namesClass = ClassUtil.getClass("com.sun.tools.javac.util.Names");
            method = ClassUtil.getMethod(namesClass, "instance", Context.class);
        }

        return ClassUtil.invoke(method, null, context);
    }
    
    /**
     * 获取构造类型标签
     * @param typeTag 类型标签
     * @return 构造类型标签
     */
    private static Object getTypeTag(TypeTag typeTag) {
        String target;

        if (Compiler.CURRENT_VERSION < Compiler.JAVA_8) {
            target = "com.sun.tools.javac.code.TypeTags";
        } else {
            target = "com.sun.tools.javac.code.TypeTag";
        }

        Class<?> targetClass = ClassUtil.getClass(target);
        return ClassUtil.getStaticField(targetClass, typeTag.name());
    }

    public Symbol.ClassSymbol getTypeElement(String className) {
        return javacElements.getTypeElement(className);
    }

    /**
     * 获取原生类型的默认返回值
     * @param primitive 类型
     * @return 返回值语句
     */
    public JCTree.JCExpression getDefaultPrimitiveValue(char primitive) {
        String pr = CheckUtil.getPrimitiveReturn(primitive);
        if (null == pr) {
            return null;
        }

        TypeTag typeTag = TypeUtil.getTypeTag(primitive);
        Object value = TreeUtil.getValue(typeTag, pr);
        if (null == value) {
            return null;
        }
        return newElement(typeTag, value);
    }

    /**
     * 创建对象类型临时变量
     * @param name 变量名
     * @param type 类名称
     * @param init 初始值
     * @return 变量元素
     */
    public JCTree.JCVariableDecl newLocalVar(String name, String type, JCTree.JCExpression init) {
        return newVar(name, 0L, type, init);
    }

    /**
     * 创建原生类型临时变量
     * @param name 变量名
     * @param typeTag 类型标签
     * @param init 初始值
     * @return 变量元素
     */
    public JCTree.JCVariableDecl newLocalVar(String name, TypeTag typeTag, JCTree.JCExpression init) {
        return newVar(name, 0L, typeTag, init);
    }

    /**
     * 创建新字段
     * @param name 变量名
     * @param flags 字段标记
     * @param type 类名称
     * @param init 初始值
     * @return 变量元素
     */
    public JCTree.JCVariableDecl newVar(String name, long flags, String type, JCTree.JCExpression init) {
        return newVar(name, flags, findClass(type), init);
    }

    /**
     * 创建原始类型字段
     * @param name 变量名
     * @param flags 字段标记
     * @param typeTag 类型标签
     * @param init 初始值
     * @return 变量元素
     */
    public JCTree.JCVariableDecl newVar(String name, long flags, TypeTag typeTag, JCTree.JCExpression init) {
        return newVar(name, flags, getPrimitiveType(typeTag), init);
    }

    /**
     * 创建临时变量
     * @param name 变量名
     * @param type 类型
     * @param init 初始值
     * @return 变量元素
     */
    private JCTree.JCVariableDecl newVar(String name, long flags, JCTree.JCExpression type, JCTree.JCExpression init) {
        return treeMaker.VarDef(treeMaker.Modifiers(flags), getName(name), type, init);
    }

    /**
     * 创建返回元素
     * @param typeTag 类型标签
     * @param value 值
     * @return 语句
     */
    public JCTree.JCReturn Return(TypeTag typeTag, Object value) {
        return treeMaker.Return(newElement(typeTag, value));
    }

    /**
     * 拼接信息
     * @param message 信息头
     * @param info 信息尾
     * @return 合并元素
     */
    public JCTree.JCExpression concatStatement(String message, JCTree.JCExpression info) {
        JCTree.JCExpression args = treeMaker.Literal(message);
        return newBinary(TypeTag.PLUS, args, info);
    }

    /**
     * 创建异常语句
     * @param exceptionTypeName 异常类型
     * @param message 异常信息
     * @return 异常语句元素
     */
    public JCTree.JCStatement newMsgThrow(String exceptionTypeName, JCTree.JCExpression message) {
        JCTree.JCExpression exceptionInstance = NewClass(exceptionTypeName, List.of(message));
        return newThrow(exceptionInstance);
    }

    /**
     * 获取属性
     * @param field 源对象
     * @param name 属性名
     * @return 属性元素
     */
    public JCTree.JCFieldAccess Select(JCTree.JCExpression field, String name) {
        return treeMaker.Select(field, getName(name));
    }

    /**
     * 获取方法
     * @param field 源对象
     * @param method 方法名
     * @param paramArgs 参数列表
     * @return 方法调用元素
     */
    public JCTree.JCMethodInvocation getMethod(JCTree.JCExpression field, String method, List<JCTree.JCExpression> paramArgs) {
        return treeMaker.Apply(null, Select(field, method), paramArgs);
    }

    /**
     * 方法调用结果赋值
     * @param giver 调用对象
     * @param accepter 接收对象
     * @param methodName 调用方法
     * @param paramArgs 参数列表
     * @return 调用赋值语句
     */
    public JCTree.JCExpressionStatement assignCallback(JCTree.JCIdent giver, JCTree.JCExpression accepter, String methodName, List<JCTree.JCExpression> paramArgs) {
        JCTree.JCExpression expression = getMethod(giver, methodName, paramArgs);
        return treeMaker.Exec(treeMaker.Assign(accepter, expression));
    }

    /**
     * 查询类引用
     * @param className 类名
     * @return 类元素
     */
    public JCTree.JCExpression findClass(String className) {
        String[] elems = className.split("\\.");

        Name name = getName(elems[0]);
        JCTree.JCExpression e = treeMaker.Ident(name);
        for (int i = 1 ; i < elems.length ; i++) {
            name = getName(elems[i]);
            e = e == null ? treeMaker.Ident(name) : treeMaker.Select(e, name);
        }
        return e;
    }

    /**
     * 获取生成器创建 Name
     * @param name 名称
     * @return 名称元素
     */
    public Name getName(String name) {
        Method method;
        if (Compiler.CURRENT_VERSION == Compiler.JAVA_6) {
            Class<?> nameClass = ClassUtil.getClass("com.sun.tools.javac.util.Name");
            Class<?> tableClass = ClassUtil.getClass("com.sun.tools.javac.util.Name$Table");
            method = ClassUtil.getMethod(nameClass, "fromString", tableClass, String.class);
            return ClassUtil.invoke(method, null, namesInstance, name);
        }

        Class<?> namesClass = ClassUtil.getClass("com.sun.tools.javac.util.Names");
        method = ClassUtil.getMethod(namesClass, "fromString", String.class);
        return ClassUtil.invoke(method, namesInstance, name);
    }

    /**
     * 获取构造函数
     * @param className 类名
     * @param params 参数
     * @return 构造函数语句
     */
    public JCTree.JCExpression NewClass(String className, List<JCTree.JCExpression> params) {
        return treeMaker.NewClass(null, TreeUtil.emptyExpression(), findClass(className), params, null);
    }

    /**
     * 获取原生类型
     * @param typeTag 类型标签
     * @return 原生类元素
     */
    public JCTree.JCPrimitiveTypeTree getPrimitiveType(TypeTag typeTag) {
        Method method;

        if (Compiler.CURRENT_VERSION < Compiler.JAVA_8) {
            method = ClassUtil.getMethod(TreeMaker.class, "TypeIdent", int.class);
        } else {
            Class<?> param = ClassUtil.getClass("com.sun.tools.javac.code.TypeTag");
            method = ClassUtil.getMethod(TreeMaker.class, "TypeIdent", param);
        }

        return ClassUtil.invoke(method, treeMaker, getTypeTag(typeTag));
    }

    /**
     * 获取类型对象
     * @param typeTag 类型标签
     * @param value 数据
     * @return 类型元素
     */
    public JCTree.JCLiteral newElement(TypeTag typeTag, Object value) {
        Method method;

        if (Compiler.CURRENT_VERSION < Compiler.JAVA_8) {
            method = ClassUtil.getMethod(TreeMaker.class, "Literal", int.class, Object.class);
        } else {
            Class<?> param = ClassUtil.getClass("com.sun.tools.javac.code.TypeTag");
            method = ClassUtil.getMethod(TreeMaker.class, "Literal", param, Object.class);
        }

        return ClassUtil.invoke(method, treeMaker, getTypeTag(typeTag), value);
    }

    /**
     * 获取 Binary 表达式
     * @param typeTag 类型标签
     * @param left 表达式
     * @param right 比较对象
     * @return 对比语句元素
     */
    public JCTree.JCExpression newBinary(TypeTag typeTag, JCTree.JCExpression left, JCTree.JCExpression right) {
        String target;
        Method method;

        if (Compiler.CURRENT_VERSION < Compiler.JAVA_8) {
            method = ClassUtil.getMethod(TreeMaker.class, "Binary", int.class,
                    JCTree.JCExpression.class, JCTree.JCExpression.class);
            target = "com.sun.tools.javac.tree.JCTree";
        } else {
            Class<?> param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree$Tag");
            method = ClassUtil.getMethod(TreeMaker.class, "Binary", param,
                    JCTree.JCExpression.class, JCTree.JCExpression.class);
            target = "com.sun.tools.javac.tree.JCTree$Tag";
        }

        Class<?> targetClass = ClassUtil.getClass(target);
        Object field = ClassUtil.getStaticField(targetClass, typeTag.name());
        return ClassUtil.invoke(method, treeMaker, field, left, right);
    }

    /**
     * 生成抛出异常语句
     * @param exceptionType 异常类型元素
     * @return 抛出异常语句
     */
    public JCTree.JCStatement newThrow(Object exceptionType) {
        Class<?> param;
        if (Compiler.CURRENT_VERSION < Compiler.JAVA_8) {
            param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree");
        } else {
            param = ClassUtil.getClass("com.sun.tools.javac.tree.JCTree$JCExpression");
        }

        Method method = ClassUtil.getMethod(TreeMaker.class, "Throw", param);
        return ClassUtil.invoke(method, treeMaker, exceptionType);
    }
}
