package io.moyada.medivh.support;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import io.moyada.medivh.util.CTreeUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 语句创建工具
 * @author xueyikang
 * @since 1.0
 **/
public class ExpressionMaker {

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
    private final Symbol.ClassSymbol stringSymbol;
    // collection
    private final Symbol.ClassSymbol collectionSymbol;
    // map
    private final Symbol.ClassSymbol mapSymbol;

    private ExpressionMaker(Context context) {
        this.treeMaker = TreeMaker.instance(context);
        this.namesInstance = CTreeUtil.newInstanceForName(context);

        this.javacElements = JavacElements.instance(context);
        this.types = Types.instance(context);

        this.nullNode = CTreeUtil.newElement(treeMaker, TypeTag.BOT, null);
        this.trueNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 1);
        this.falseNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 0);
        this.zeroIntNode = CTreeUtil.newElement(treeMaker, TypeTag.INT, 0);
        this.emptyCh = CTreeUtil.newElement(treeMaker, TypeTag.CHAR, (int) ' ');

        collectionSymbol = javacElements.getTypeElement(Collection.class.getName());
        mapSymbol = javacElements.getTypeElement(Map.class.getName());
        stringSymbol = javacElements.getTypeElement(CharSequence.class.getName());

        RegulationExecutor.init(this);
    }

    public static ExpressionMaker newInstance(Context context) {
        return new ExpressionMaker(context);
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
        return newVar(name, flags, CTreeUtil.getPrimitiveType(treeMaker, typeTag), init);
    }

    /**
     * 创建临时变量
     * @param name 变量名
     * @param type 类型
     * @param init 初始值
     * @return 变量元素
     */
    private JCTree.JCVariableDecl newVar(String name, long flags, JCTree.JCExpression type, JCTree.JCExpression init) {
        return treeMaker.VarDef(treeMaker.Modifiers(flags),
                CTreeUtil.getName(namesInstance, name), type, init);
    }

    /**
     * 创建返回元素
     * @param typeTag 类型标签
     * @param value 值
     * @return 语句
     */
    public JCTree.JCReturn Return(TypeTag typeTag, Object value) {
        return treeMaker.Return(CTreeUtil.newElement(treeMaker, typeTag, value));
    }

    /**
     * 拼接信息
     * @param message 信息头
     * @param info 信息尾
     * @return 合并元素
     */
    public JCTree.JCExpression concatStatement(String message, JCTree.JCExpression info) {
        JCTree.JCExpression args = treeMaker.Literal(message);
        return CTreeUtil.newBinary(treeMaker, TypeTag.PLUS, args, info);
    }

    /**
     * 创建异常语句
     * @param exceptionTypeName 异常类型
     * @param message 异常信息
     * @return 异常语句元素
     */
    public JCTree.JCStatement newMsgThrow(String exceptionTypeName, JCTree.JCExpression message) {
        JCTree.JCExpression exceptionType = findClass(exceptionTypeName);
        JCTree.JCExpression exceptionInstance = treeMaker.NewClass(null, CTreeUtil.emptyParam(), exceptionType, List.of(message), null);
        return CTreeUtil.newThrow(treeMaker, exceptionInstance);
    }

    /**
     * 获取属性
     * @param field 源对象
     * @param name 属性名
     * @return 属性元素
     */
    public JCTree.JCFieldAccess Select(JCTree.JCExpression field, String name) {
        return treeMaker.Select(field, CTreeUtil.getName(namesInstance, name));
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

        Name name = CTreeUtil.getName(namesInstance, elems[0]);
        JCTree.JCExpression e = treeMaker.Ident(name);
        for (int i = 1 ; i < elems.length ; i++) {
            name = CTreeUtil.getName(namesInstance, elems[i]);
            e = e == null ? treeMaker.Ident(name) : treeMaker.Select(e, name);
        }
        return e;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public JavacElements getJavacElements() {
        return javacElements;
    }

    public Object getNamesInstance() {
        return namesInstance;
    }

    public Types getTypes() {
        return types;
    }

    public Symbol.ClassSymbol getCollectionSymbol() {
        return collectionSymbol;
    }

    public Symbol.ClassSymbol getMapSymbol() {
        return mapSymbol;
    }

    public Symbol.ClassSymbol getStringSymbol() {
        return stringSymbol;
    }
}
