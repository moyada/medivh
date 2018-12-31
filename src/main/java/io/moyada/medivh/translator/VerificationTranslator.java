package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.Check;
import io.moyada.medivh.annotation.Verify;
import io.moyada.medivh.util.*;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 校验逻辑生成器
 * 对定义 @link{Verify} 注解的方法增加入参合法性校验
 * @author xueyikang
 * @since 1.0
 **/
public class VerificationTranslator extends BaseTranslator {

    // 校验规则对象
    private Collection<String> ruleClass;

    public VerificationTranslator(Context context, Collection<? extends javax.lang.model.element.Element> ruleClass, Messager messager) {
        super(context, messager);

        this.ruleClass = new ArrayList<String>(ruleClass.size());
        for (javax.lang.model.element.Element rule : ruleClass) {
            this.ruleClass.add(rule.asType().toString());
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
        super.visitMethodDef(methodDecl);

        // 无需校验的方法
        if (isJump(methodDecl)) {
            return;
        }

        Verify verify = methodDecl.sym.getAnnotation(Verify.class);
        if (null == verify) {
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Build Verify logic for " + methodDecl.sym.getEnclosingElement().asType().toString()
                + "." + methodDecl.name.toString() + "()");

        // 获取前置信息
        String actionInfo = Element.ACTION_INFO;
        // 返回类型
        String returnTypeName = CTreeUtil.getReturnTypeName(methodDecl);

        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        // 创建临时变量提取引用
        JCTree.JCVariableDecl msg = newVar(SystemUtil.getTmpVar(verify), 0L, String.class.getName(), null);
        JCTree.JCIdent ident = treeMaker.Ident(msg.name);

        CheckInfo checkInfo;
        for (JCTree.JCVariableDecl param : methodDecl.params) {
            Check annotation = param.sym.getAnnotation(Check.class);
            if (null == annotation) {
                continue;
            }


            // 选择抛出异常或者返回类型为 void
            if (annotation.throwable() || null == returnTypeName) {
                // 默认异常类
                Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Check.class.getName());
                String exception = getException(annotationAttr);

                String message = annotation.message();
                if (message.equals("")) {
                    message = Element.MESSAGE + actionInfo;
                } else {
                    message += actionInfo;
                }
                checkInfo = new CheckInfo(exception, message, annotation.nullable());
            } else {
                JCTree.JCStatement statement = getReturn(returnTypeName, annotation.returnValue());
                if (statement == null) {
                    return;
                }
                checkInfo = new CheckInfo(statement, annotation.nullable());
            }

            buildLogic(statements, ident, param, checkInfo);
        }

        List<JCTree.JCStatement> oldStatements = methodDecl.body.stats;
        int size = oldStatements.size();
        for (int i = 0; i < size; i++) {
            statements.append(oldStatements.get(i));
        }
        statements.prepend(msg);

        // 获取代码块
        JCTree.JCBlock body = getBlock(statements);
        methodDecl.body = body;
        this.result = newMethod(methodDecl, body);
    }

    /**
     * 跳过 接口、抽象、无注解 方法
     * @param methodDecl
     * @return
     */
    private boolean isJump(JCTree.JCMethodDecl methodDecl) {
        if ((methodDecl.sym.getEnclosingElement().flags() & Flags.INTERFACE) != 0) {
            return true;
        }
        if ((methodDecl.getModifiers().flags & Flags.ABSTRACT) != 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取返回对象语句
     * @param returnTypeName
     * @return
     */
    private JCTree.JCStatement getReturn(String returnTypeName, String[] values) {
        JCTree.JCExpression returnValue;
        int length = values.length;

        if (length == 0) {
            return treeMaker.Return(getEmptyType(returnTypeName));
        } else if (length == 1) {
            String value = values[0];
            if (value.isEmpty()) {
                // 返回空构造方法
                return treeMaker.Return(getEmptyType(returnTypeName));
            }

            if (value.equalsIgnoreCase("null")) {
                // 返回空对象
                if (TypeUtil.isPrimitive(returnTypeName)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "cannot return <nulltype> value to " + returnTypeName);
                }
                returnValue = nullNode;
                return treeMaker.Return(returnValue);
            }

            // 基本类型
            TypeTag baseType = TypeUtil.getBaseType(returnTypeName);
            if (null != baseType) {
                Object tagValue = CTreeUtil.getValue(baseType, value);
                if (null == tagValue) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "cannot convert " + value + " to " + returnTypeName);
                }
                returnValue = CTreeUtil.newElement(treeMaker, baseType, tagValue);
                return treeMaker.Return(returnValue);
            }
        }

        JCTree.JCExpression returnType = findClass(returnTypeName);
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(returnTypeName);
        List<JCTree.JCExpression> paramType = getParamType(classSymbol, values);
        if (null == paramType) {
            messager.printMessage(Diagnostic.Kind.ERROR, "cannot find match param constructor with " + Arrays.toString(values));
        }
        returnValue = treeMaker.NewClass(null, CTreeUtil.emptyParam(), returnType, paramType, null);
        return treeMaker.Return(returnValue);
    }


    /**
     * 获取配置异常
     * @param annotationAttr
     * @return
     */
    private String getException(Attribute.Compound annotationAttr) {
        String exception = CTreeUtil.getAnnotationValue(annotationAttr, "invalid()");
        if (null == exception) {
            exception = IllegalArgumentException.class.getName();
        } else {
            if (!checkException(exception)) {
                messager.printMessage(Diagnostic.Kind.ERROR, exception + " must be provide a String constructor!");
            }
        }
        return exception;
    }

    /**
     * 构建检验逻辑
     * @param statements
     * @param ident
     * @param param
     * @param checkInfo
     * @return
     */
    private void buildLogic(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent ident,
                            JCTree.JCVariableDecl param, CheckInfo checkInfo) {
        // 获取参数引用
        JCTree.JCIdent var = treeMaker.Ident(param.name);

        String name = CTreeUtil.getOriginalTypeName(param.sym);
        if(TypeUtil.isPrimitive(name)) {
            return;
        }

        if (!checkInfo.nullable) {
            addNotNullCheck(statements, var, checkInfo);
        }

        if (ruleClass.contains(name)) {
            addInvalidStatement(statements, ident, var, checkInfo);
        }
    }

    /**
     * 添加非空校验
     * @param statements
     * @param field
     * @param info
     * @return
     */
    private void addNotNullCheck(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent field, CheckInfo info) {
        JCTree.JCStatement statement;

        JCTree.JCExpression check = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, field, nullNode);

        if (info.isThrow()) {
            JCTree.JCLiteral message = CTreeUtil.newElement(treeMaker, TypeTag.CLASS, info.info + field.name + " is null");
            statement = newMsgThrow(message, info.exceptionName);
        } else {
            statement = info.returnValue;
        }

        statements.append(treeMaker.If(check, statement, null));
    }

    /**
     * 增加校验逻辑
     * @param statements
     * @param ident
     * @param field
     * @param info
     * @return
     */
    private void addInvalidStatement(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent ident,
                                     JCTree.JCIdent field, CheckInfo info) {
        JCTree.JCStatement statement;

        // 将校验结果赋值给临时变量
        JCTree.JCExpression expression = getMethod(field, Element.METHOD_NAME, CTreeUtil.emptyParam());
        JCTree.JCExpressionStatement exec = execMethod(treeMaker.Assign(ident, expression));

        if (info.isThrow()) {
            // 抛出异常语句
            JCTree.JCExpression message = concatStatement(ident, info.info);
            statement = newMsgThrow(message, info.exceptionName);
        } else {
            statement = info.returnValue;
        }

        // 校验结果
        JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.NE, ident, nullNode);
        JCTree.JCIf proc = treeMaker.If(condition, statement, null);

        // 赋值校验子逻辑
        ListBuffer<JCTree.JCStatement> tempStatement = CTreeUtil.newStatement();
        tempStatement.append(exec);
        tempStatement.append(proc);

        if (info.nullable) {
            // 包装判空逻辑
            JCTree.JCExpression nullCheck = CTreeUtil.newExpression(treeMaker, TypeTag.NE, field, nullNode);
            statements.append(treeMaker.If(nullCheck, getBlock(tempStatement), null));
        } else {
            statements.append(getBlock(tempStatement));
        }
    }

    /**
     * 拼接信息
     * @param info
     * @param message
     * @return
     */
    private JCTree.JCExpression concatStatement(JCTree.JCExpression info, String message) {
        JCTree.JCExpression args = treeMaker.Literal(message);
        return CTreeUtil.newExpression(treeMaker, TypeTag.PLUS, args, info);
    }

//    private JCTree.JCMethodInvocation concatStatement(JCTree.JCExpression info, String message) {
//        JCTree.JCExpression args = treeMaker.Literal(message);
//        return getMethod(args, "concat", List.of(info));
//    }

    /**
     * 创建新方法
     * @param methodDecl
     * @param body
     * @return
     */
    private JCTree.JCMethodDecl newMethod(JCTree.JCMethodDecl methodDecl, JCTree.JCBlock body) {
        return treeMaker.MethodDef(methodDecl.mods,
                methodDecl.name,
                methodDecl.restype,
                methodDecl.typarams,
                methodDecl.params,
                methodDecl.thrown,
                body, methodDecl.defaultValue);
    }

    /**
     * 检验逻辑
     */
    class CheckInfo {

        private JCTree.JCStatement returnValue;

        private String exceptionName;

        private String info;

        private boolean nullable;

        CheckInfo(JCTree.JCStatement returnValue, boolean nullable) {
            this.returnValue = returnValue;
            this.nullable = nullable;
        }

        CheckInfo(String exceptionName, String info, boolean nullable) {
            this.exceptionName = exceptionName;
            this.info = info;
            this.nullable = nullable;
        }

        private boolean isThrow() {
            return returnValue == null;
        }
    }
}
