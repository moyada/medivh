package io.moyada.medivh.visitor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.TypeTag;
import io.moyada.medivh.util.TreeUtil;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * 工具方法生成器
 * @author xueyikang
 * @since 1.2.0
 **/
public class UtilMethodTranslator extends BaseTranslator {

    // 根类名
    private String className;

    private PosScanner posScanner;

    public UtilMethodTranslator(SyntaxTreeMaker syntaxTreeMaker, Messager messager, String className) {
        super(syntaxTreeMaker, messager);
        this.className = className;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        String typeName = TreeUtil.getOriginalTypeName(jcClassDecl.sym);
        // 过滤内部类
        if (!typeName.equals(className)) {
            return;
        }

//        startPos = jcClassDecl.getPreferredPosition();
//        jcClassDecl.setPos(startPos + 10);
        posScanner = new PosScanner(jcClassDecl);

        makeIsBlankMethod(jcClassDecl, ElementOptions.BLANK_METHOD);
        this.result = jcClassDecl;
    }

    /**
     * 设置空白字符串校验方法
     * @param jcClassDecl 类节点
     * @param methodName 方法名
     */
    private void makeIsBlankMethod(JCTree.JCClassDecl jcClassDecl, String methodName) {
        JCTree.JCMethodDecl isBlankMethod = createIsBlankMethod(methodName);
        if (null == isBlankMethod) {
            return;
        }

        jcClassDecl.defs = jcClassDecl.defs.append(isBlankMethod);
        ElementOptions.UTIL_CLASS = className;
        messager.printMessage(Diagnostic.Kind.NOTE, "Create method \"" + methodName + "\" in " + className);
    }

    /**
     * 创建空包字符串校验方法
     * @param methodName 方法名
     * @return 返回新方法
     */
    private JCTree.JCMethodDecl createIsBlankMethod(String methodName) {
        JCTree.JCReturn returnTrue = treeMaker.Return(syntaxTreeMaker.trueNode);
        JCTree.JCReturn returnFalse = treeMaker.Return(syntaxTreeMaker.falseNode);

        // define String str parameter
        JCTree.JCVariableDecl var = syntaxTreeMaker.newVar("str", Flags.PARAMETER, CharSequence.class.getName(), null);
        JCTree.JCIdent str = treeMaker.Ident(var.name);

        ListBuffer<JCTree.JCStatement> statements = TreeUtil.newStatement();

        // int length = str.length();
        JCTree.JCMethodInvocation getLength = syntaxTreeMaker.getMethod(str, "length", TreeUtil.emptyParam());
        JCTree.JCVariableDecl newInt = syntaxTreeMaker.newLocalVar("length", TypeTag.INT, getLength);
        JCTree.JCIdent length = treeMaker.Ident(newInt.name);

        statements.append(newInt);

        // if (length == 0) { return true; }
        JCTree.JCExpression isZero = syntaxTreeMaker.newBinary(TypeTag.EQ, length, syntaxTreeMaker.zeroIntNode);
        JCTree.JCIf ifReturn = treeMaker.If(isZero, returnTrue, null);

        statements.append(ifReturn);

        // char ch;
        JCTree.JCVariableDecl varChar = syntaxTreeMaker.newLocalVar("ch", TypeTag.CHAR, null);

        statements.append(varChar);

        // for (int i = 0; i < length; i++) { body ... }

        // int i = 0;
        JCTree.JCVariableDecl init = syntaxTreeMaker.newLocalVar("i", TypeTag.INT, syntaxTreeMaker.zeroIntNode);
        JCTree.JCExpression vari = treeMaker.Ident(init.name);
        // i < length
        JCTree.JCExpression condition = syntaxTreeMaker.newBinary(TypeTag.LT, vari, length);
        // i++
        JCTree.JCLiteral onePlus = syntaxTreeMaker.newElement(TypeTag.INT, 1);
        JCTree.JCExpression step = syntaxTreeMaker.newBinary(TypeTag.PLUS, vari, onePlus);

        // body start
        // ch = str.charAt(i);
        ListBuffer<JCTree.JCStatement> body = TreeUtil.newStatement();
        JCTree.JCIdent ch = treeMaker.Ident(varChar.name);
        List<JCTree.JCExpression> paramArgs = List.of(vari);
        JCTree.JCExpressionStatement charAt = syntaxTreeMaker.assignCallback(str,  ch,"charAt", paramArgs);

        // if (ch != ' ') { return false; }
        JCTree.JCExpression isNotEmtyp = syntaxTreeMaker.newBinary(TypeTag.NE, ch, syntaxTreeMaker.emptyCh);
        JCTree.JCIf notEqualsReturn = treeMaker.If(isNotEmtyp, returnFalse, null);
        // body end

        body.append(charAt);
        body.append(notEqualsReturn);

        JCTree.JCForLoop loop = treeMaker.ForLoop(List.of((JCTree.JCStatement) init), condition,
                List.of(treeMaker.Exec(treeMaker.Assign(vari, step))), getBlock(body));
        statements.append(loop);

        statements.append(returnTrue);

        return createPublicStaticMethod(methodName, List.of(var), getBlock(statements));
    }

    /**
     * 创建静态公共方法
     * @param methodName 方法名
     * @param var 方法参数
     * @param body 方法内容
     * @return 方法元素
     */
    private JCTree.JCMethodDecl createPublicStaticMethod(String methodName, List<JCTree.JCVariableDecl> var, JCTree.JCBlock body) {
        TreeUtil.visit(var, posScanner);

        List<JCTree.JCTypeParameter> param = List.nil();
        List<JCTree.JCExpression> thrown = List.nil();
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                syntaxTreeMaker.getName(methodName),
                syntaxTreeMaker.getPrimitiveType(TypeTag.BOOLEAN),
                param, var, thrown,
                body, null);
    }
}
