package cn.moyada.function.validator.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;

/**
 * @author xueyikang
 * @since 1.0
 **/
class BaseTranslator extends TreeTranslator {

    protected final Messager messager;

    final TreeMaker treeMaker;
    final JavacElements javacElements;
    final Names names;

    // null 表达式
    final JCTree.JCLiteral nullNode;

    // true 表达式
    final JCTree.JCLiteral trueNode;

    // false 表达式
    final JCTree.JCLiteral falseNode;

    BaseTranslator(Context context, Messager messager) {
        this.messager = messager;

        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.javacElements = JavacElements.instance(context);

        this.nullNode = treeMaker.Literal(TypeTag.BOT, null);
        this.trueNode = treeMaker.Literal(TypeTag.BOOLEAN, 1);
        this.falseNode = treeMaker.Literal(TypeTag.BOOLEAN, 0);
    }

    /**
     * 获取表达式的代码块
     * @param statements
     * @return
     */
    protected JCTree.JCBlock getBlock(ListBuffer<JCTree.JCStatement> statements) {
        return treeMaker.Block(0, statements.toList());
    }

    /**
     * 创建新变量
     * @param name
     * @param flags
     * @param type
     * @param init
     * @return
     */
    protected JCTree.JCVariableDecl newVar(String name, long flags, String type, JCTree.JCExpression init) {
        return treeMaker.VarDef(treeMaker.Modifiers(flags),
                names.fromString(name),
                findClass(type), init);
    }

    /**
     * 执行调用
     * @param expression
     * @return
     */
    protected JCTree.JCExpressionStatement execMethod(JCTree.JCExpression expression) {
        return treeMaker.Exec(expression);
    }

    /**
     * 获取方法
     * @param field
     * @param method
     * @param param
     * @return
     */
    protected JCTree.JCMethodInvocation getMethod(JCTree.JCExpression field, String method, List<JCTree.JCExpression> param) {
        return treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(field, names.fromString(method)),
                        param
                );
    }

    /**
     * 创建异常语句
     * @param message
     * @param exceptionTypeName
     * @return
     */
    protected JCTree.JCStatement newMsgThrow(JCTree.JCExpression message, String exceptionTypeName) {
        JCTree.JCExpression exceptionType = findClass(exceptionTypeName);

        List<JCTree.JCExpression> jceBlank = List.nil();
        JCTree.JCExpression exceptionInstance = treeMaker.NewClass(null, jceBlank, exceptionType, List.of(message), null);

        return treeMaker.Throw(exceptionInstance);
    }

    /**
     * 检测异常类是否包含 String 构造函数
     * @param exceptionTypeName
     * @return
     */
    protected boolean checkException(String exceptionTypeName) {
        Symbol.ClassSymbol typeElement = javacElements.getTypeElement(exceptionTypeName);
        for (Symbol element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            List<Symbol.VarSymbol> parameters = ((Symbol.MethodSymbol) element).getParameters();
            if (parameters.size() != 1) {
                continue;
            }
            if (parameters.get(0).asType().toString().equals("java.lang.String")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询类引用
     * @param className
     * @return
     */
    protected JCTree.JCExpression findClass(String className) {
        String[] elems = className.split("\\.");

        JCTree.JCExpression e = treeMaker.Ident(names.fromString(elems[0]));
        for (int i = 1 ; i < elems.length ; i++) {
            e = e == null ? treeMaker.Ident(names.fromString(elems[i])) : treeMaker.Select(e, names.fromString(elems[i]));
        }

        return e;
    }
}
