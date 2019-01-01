package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.Nullable;
import io.moyada.medivh.annotation.Return;
import io.moyada.medivh.annotation.Throw;
import io.moyada.medivh.annotation.Variable;
import io.moyada.medivh.util.*;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 校验逻辑生成器
 * 对定义的注解的方法增加入参合法性校验
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

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Build Verify logic for " + methodDecl.sym.getEnclosingElement().asType().toString()
                + "." + methodDecl.name.toString() + "()");

        // 获取前置信息
        String actionInfo = Element.ACTION_INFO;
        // 返回类型
        String returnTypeName = CTreeUtil.getReturnTypeName(methodDecl);

        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        // 创建临时变量提取引用
        JCTree.JCVariableDecl msg = newVar(SystemUtil.getTmpVar(methodDecl.sym.getAnnotation(Variable.class)),
                0L, String.class.getName(), null);
        JCTree.JCIdent ident = treeMaker.Ident(msg.name);

        for (JCTree.JCVariableDecl param : methodDecl.params) {
            CheckData checkData = getCheckInfo(param, returnTypeName, actionInfo);
            if (null == checkData) {
                continue;
            }
            buildLogic(statements, ident, param, checkData);
        }
        if (statements.isEmpty()) {
            return;
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
     * 获取校验信息
     * @param param
     * @param returnTypeName
     * @param actionInfo
     * @return
     */
    private CheckData getCheckInfo(JCTree.JCVariableDecl param, String returnTypeName, String actionInfo) {
        boolean nullable = param.sym.getAnnotation(Nullable.class) != null;

        Throw throwAttr = param.sym.getAnnotation(Throw.class);
        if (null != throwAttr) {
            // 默认异常类
            Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Throw.class.getName());
            String exception = getException(annotationAttr);

            String message = throwAttr.message();
            if (message.equals("")) {
                message = Element.MESSAGE + actionInfo;
            } else {
                message += actionInfo;
            }

            return new CheckData(exception, message, nullable);
        }

        Return returnAttr = param.sym.getAnnotation(Return.class);
        if (null != returnAttr && returnTypeName != null) {
            Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Return.class.getName());
            // 有指定类型
            String type = CTreeUtil.getAnnotationValue(annotationAttr, "type()");

            if (null != type && !type.equals(Object.class.getName())) {
                type = TypeUtil.getWrapperType(type);

                if (isSubClass(type, returnTypeName)) {
                    returnTypeName = type;
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, type + " cannot convert to " + returnTypeName);
                }
            }

            JCTree.JCStatement statement = getReturn(returnTypeName, returnAttr.value());
            if (statement == null) {
                return null;
            }
            return new CheckData(statement, nullable);
        }

        return null;
    }

    /**
     * 获取返回对象语句
     * @param returnTypeName
     * @return
     */
    private JCTree.JCStatement getReturn(String returnTypeName, String[] values) {
        JCTree.JCExpression returnValue;

        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(returnTypeName);
        boolean errorType;
        if (null == classSymbol) {
            errorType = false;
        } else {
            errorType = CTreeUtil.isAbsOrInter(classSymbol.flags());
        }

        if (Element.isReturnNull(values)) {
            // 返回空对象
            if (TypeUtil.isPrimitive(returnTypeName)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "cannot return <nulltype> value to " + returnTypeName);
            }
            returnValue = nullNode;
            return treeMaker.Return(returnValue);
        }

        if (errorType) {
            messager.printMessage(Diagnostic.Kind.ERROR, "cannot find constructor from abstract or interface type => " + returnTypeName);
            return null;
        }

        int length = values.length;
        if (length == 0) {
            // 返回空构造方法
            return treeMaker.Return(getEmptyType(returnTypeName));
        } else if (length == 1) {
            String value = values[0];
            if (value.isEmpty()) {
                // 返回空构造方法
                return treeMaker.Return(getEmptyType(returnTypeName));
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
        String exception = CTreeUtil.getAnnotationValue(annotationAttr, "value()");
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
     * @param checkData
     * @return
     */
    private void buildLogic(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent ident,
                            JCTree.JCVariableDecl param, CheckData checkData) {
        // 获取参数引用
        JCTree.JCIdent var = treeMaker.Ident(param.name);

        String name = CTreeUtil.getOriginalTypeName(param.sym);
        if(TypeUtil.isPrimitive(name)) {
            return;
        }

        if (!checkData.nullable) {
            addNotNullCheck(statements, var, checkData);
        }

        if (ruleClass.contains(name)) {
            addInvalidStatement(statements, ident, var, checkData);
        }
    }

    /**
     * 添加非空校验
     * @param statements
     * @param field
     * @param info
     * @return
     */
    private void addNotNullCheck(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent field, CheckData info) {
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
                                     JCTree.JCIdent field, CheckData info) {
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
    class CheckData {

        private JCTree.JCStatement returnValue;

        private String exceptionName;

        private String info;

        private boolean nullable;

        CheckData(JCTree.JCStatement returnValue, boolean nullable) {
            this.returnValue = returnValue;
            this.nullable = nullable;
        }

        CheckData(String exceptionName, String info, boolean nullable) {
            this.exceptionName = exceptionName;
            this.info = info;
            this.nullable = nullable;
        }

        private boolean isThrow() {
            return returnValue == null;
        }
    }
}
