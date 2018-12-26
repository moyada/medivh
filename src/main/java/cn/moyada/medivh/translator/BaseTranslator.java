package cn.moyada.medivh.translator;

import cn.moyada.medivh.util.CTreeUtil;
import cn.moyada.medivh.util.TypeTag;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;
import java.util.Collection;
import java.util.Map;

/**
 * @author xueyikang
 * @since 1.0
 **/
class BaseTranslator extends TreeTranslator {

    protected final Messager messager;

    final TreeMaker treeMaker;
    final JavacElements javacElements;
    final Object namesInstance;
    final Types types;

    // null 表达式
    final JCTree.JCLiteral nullNode;

    // true 表达式
    final JCTree.JCLiteral trueNode;

    // false 表达式
    final JCTree.JCLiteral falseNode;

    // collection
    final Symbol.ClassSymbol collectionSymbol;
    final Symbol.ClassSymbol mapSymbol;

    BaseTranslator(Context context, Messager messager) {
        this.messager = messager;

        this.treeMaker = TreeMaker.instance(context);
        this.namesInstance = CTreeUtil.newNames(context);

        this.javacElements = JavacElements.instance(context);
        this.types = Types.instance(context);

        this.nullNode = CTreeUtil.newElement(treeMaker, TypeTag.BOT, null);
        this.trueNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 1);
        this.falseNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 0);

        collectionSymbol = javacElements.getTypeElement(Collection.class.getName());
        mapSymbol = javacElements.getTypeElement(Map.class.getName());
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
     * 是否属于集合或子类
     * @param className
     * @return
     */
    protected boolean isCollection(String className) {
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(className);
        // primitive 类型
        if (null == classSymbol) {
            return false;
        }

        if (classSymbol.isInterface()) {
            return isCollection(classSymbol);
        } else {
            List<Type> interfaces = classSymbol.getInterfaces();
            int size = interfaces.size();
            if (size == 0) {
                return false;
            }

            Symbol.TypeSymbol typeSymbol;
            for (int i = 0; i < size; i++) {
                typeSymbol = interfaces.get(i).tsym;
                if (isCollection(typeSymbol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCollection(Symbol typeSymbol) {
        return isInstanceOf(typeSymbol, collectionSymbol) || isInstanceOf(typeSymbol, mapSymbol);
    }

    private boolean isInstanceOf(Symbol typeSymbol, Symbol.ClassSymbol classSymbol) {
        return typeSymbol.isSubClass(classSymbol, types);
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
                CTreeUtil.fromString(namesInstance, name),
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
     * 获取属性
     * @param field
     * @param name
     * @return
     */
    protected JCTree.JCFieldAccess getField(JCTree.JCExpression field, String name) {
        return treeMaker.Select(field, CTreeUtil.fromString(namesInstance, name));
    }

    /**
     * 获取方法
     * @param field
     * @param method
     * @param param
     * @return
     */
    protected JCTree.JCMethodInvocation getMethod(JCTree.JCExpression field, String method, List<JCTree.JCExpression> param) {
        return treeMaker.Apply(CTreeUtil.emptyParam(),
                        getField(field, method),
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

        JCTree.JCExpression exceptionInstance = treeMaker.NewClass(null, CTreeUtil.emptyParam(), exceptionType, List.of(message), null);

        return CTreeUtil.newThrow(treeMaker, exceptionInstance);
    }

    /**
     * 查询类引用
     * @param className
     * @return
     */
    protected JCTree.JCExpression findClass(String className) {
        String[] elems = className.split("\\.");

        Name name = CTreeUtil.fromString(namesInstance, elems[0]);
        JCTree.JCExpression e = treeMaker.Ident(name);
        for (int i = 1 ; i < elems.length ; i++) {
            name = CTreeUtil.fromString(namesInstance, elems[i]);
            e = e == null ? treeMaker.Ident(name) : treeMaker.Select(e, name);
        }

        return e;
    }
}
