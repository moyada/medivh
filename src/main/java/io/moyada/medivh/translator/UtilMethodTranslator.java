package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.ExpressionMaker;
import io.moyada.medivh.support.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

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

    public UtilMethodTranslator(ExpressionMaker expressionMaker, Messager messager, String className) {
        super(expressionMaker, messager);
        this.className = className;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        String typeName = CTreeUtil.getOriginalTypeName(jcClassDecl.sym);
        // 过滤内部类
        if (!typeName.equals(className)) {
            return;
        }

        makeIsBlankMethod(jcClassDecl, "isBlank");
        this.result = jcClassDecl;
    }

    /**
     * 设置空白字符串校验方法
     * @param jcClassDecl 类节点
     * @param methodName 方法名
     */
    private void makeIsBlankMethod(JCTree.JCClassDecl jcClassDecl, String methodName) {
        // 已有指定方法
        if (ElementOptions.BLANK_METHOD != null) {
            return;
        }

        if (createIsBlankMethod(jcClassDecl, methodName)) {
            ElementOptions.BLANK_METHOD = new String[]{className, methodName};
            messager.printMessage(Diagnostic.Kind.NOTE, "Create " + methodName + " method in " + className);
        }
    }

    /**
     * 创建空包字符串校验方法
     * @param jcClassDecl 类节点
     * @param methodName 方法名
     * @return 是否创建成功
     */
    private boolean createIsBlankMethod(JCTree.JCClassDecl jcClassDecl, String methodName) {
        JCTree.JCReturn returnTrue = treeMaker.Return(expressionMaker.trueNode);
        JCTree.JCReturn returnFalse = treeMaker.Return(expressionMaker.falseNode);

        // define String str parameter
        JCTree.JCVariableDecl var = expressionMaker.newVar("str", Flags.PARAMETER, CharSequence.class.getName(), null);
        JCTree.JCIdent str = treeMaker.Ident(var.name);

        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        // int length = str.length();
        JCTree.JCMethodInvocation getLength = expressionMaker.getMethod(str, "length", CTreeUtil.emptyParam());
        JCTree.JCVariableDecl newInt = expressionMaker.newLocalVar("length", TypeTag.INT, getLength);
        JCTree.JCIdent length = treeMaker.Ident(newInt.name);

        statements.append(newInt);

        // if (length == 0) { return true; }
        JCTree.JCExpression isZero = CTreeUtil.newBinary(treeMaker, TypeTag.EQ, length, expressionMaker.zeroIntNode);
        JCTree.JCIf ifReturn = treeMaker.If(isZero, returnTrue, null);

        statements.append(ifReturn);

        // char ch;
        JCTree.JCVariableDecl varChar = expressionMaker.newLocalVar("ch", TypeTag.CHAR, null);

        statements.append(varChar);

        // for (int i = 0; i < length; i++) { body ... }

        // int i = 0;
        JCTree.JCVariableDecl init = expressionMaker.newLocalVar("i", TypeTag.INT, expressionMaker.zeroIntNode);
        JCTree.JCExpression vari = treeMaker.Ident(init.name);
        // i < length
        JCTree.JCExpression condition = CTreeUtil.newBinary(treeMaker, TypeTag.LT, vari, length);
        // i++
        JCTree.JCLiteral onePlus = CTreeUtil.newElement(treeMaker, TypeTag.INT, 1);
        JCTree.JCExpression step = CTreeUtil.newBinary(treeMaker, TypeTag.PLUS, vari, onePlus);

        // body start
        // ch = str.charAt(i);
        ListBuffer<JCTree.JCStatement> body = CTreeUtil.newStatement();
        JCTree.JCIdent ch = treeMaker.Ident(varChar.name);
        List<JCTree.JCExpression> paramArgs = List.of(vari);
        JCTree.JCExpressionStatement charAt = expressionMaker.assignCallback(str,  ch,"charAt", paramArgs);

        // if (ch != ' ') { return false; }
        JCTree.JCExpression isNotEmtyp = CTreeUtil.newBinary(treeMaker, TypeTag.NE, ch, expressionMaker.emptyCh);
        JCTree.JCIf notEqualsReturn = treeMaker.If(isNotEmtyp, returnFalse, null);

        // body end

        body.append(charAt);
        body.append(notEqualsReturn);

        JCTree.JCForLoop loop = treeMaker.ForLoop(List.of((JCTree.JCStatement) init), condition,
                List.of(treeMaker.Exec(treeMaker.Assign(vari, step))), getBlock(body));
        statements.append(loop);

        statements.append(returnTrue);

        JCTree.JCMethodDecl isBlank = createPublicStaticMethod(methodName, List.of(var), getBlock(statements));

        // 刷新类信息
        jcClassDecl.defs = jcClassDecl.defs.append(isBlank);
        return true;
    }

    /**
     * 创建静态公共方法
     * @param methodName 方法名
     * @param var 方法参数
     * @param body 方法内容
     * @return 方法元素
     */
    private JCTree.JCMethodDecl createPublicStaticMethod(String methodName, List<JCTree.JCVariableDecl> var, JCTree.JCBlock body) {
        List<JCTree.JCTypeParameter> param = List.nil();
        List<JCTree.JCExpression> thrown = List.nil();
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                CTreeUtil.getName(namesInstance, methodName),
                CTreeUtil.getPrimitiveType(treeMaker, TypeTag.BOOLEAN),
                param, var, thrown,
                body, null);
    }
}
