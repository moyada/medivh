package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.core.*;
import io.moyada.medivh.regulation.*;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.TypeUtil;

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

    public VerificationTranslator(MakerContext makerContext, Collection<? extends javax.lang.model.element.Element> ruleClass, Messager messager) {
        super(makerContext, messager);

        this.ruleClass = new ArrayList<String>(ruleClass.size());
        for (javax.lang.model.element.Element rule : ruleClass) {
            this.ruleClass.add(rule.asType().toString());
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
        super.visitMethodDef(methodDecl);

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Build Verify logic for "
                + methodDecl.sym.getEnclosingElement().asType().toString() + "." + methodDecl.name.toString() + "()");

        // 返回类型
        String returnTypeName = CTreeUtil.getReturnTypeName(methodDecl);

        // 创建临时变量提取引用
        JCTree.JCVariableDecl msg = newVar(Element.getTmpVar(CTreeUtil.getAnnotation(methodDecl.sym, Variable.class)),
                0L, String.class.getName(), null);
        JCTree.JCIdent ident = treeMaker.Ident(msg.name);

        ListBuffer<JCTree.JCStatement> statements = buildLogic(methodDecl.params, returnTypeName, ident);
        if (statements.isEmpty()) {
            return;
        }

        statements.prepend(msg);
        // 加入原始逻辑
        List<JCTree.JCStatement> oldStatements = methodDecl.body.stats;
        int size = oldStatements.size();
        for (int i = 0; i < size; i++) {
            statements.append(oldStatements.get(i));
        }

        JCTree.JCBlock body = getBlock(statements);
        methodDecl.body = body;
        this.result = newMethod(methodDecl, body);
    }

    /**
     * 构建校验逻辑
     * @param params
     * @param returnTypeName
     * @param msgField
     * @return
     */
    public ListBuffer<JCTree.JCStatement> buildLogic(List<JCTree.JCVariableDecl> params,
                                                     String returnTypeName, JCTree.JCIdent msgField) {

        // 建立校验逻辑
        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        for (JCTree.JCVariableDecl param : params) {
            JCTree.JCStatement paramStatements = getParamStatements(param, returnTypeName, msgField);
            if (null == paramStatements) {
                continue;
            }
            statements.append(paramStatements);
        }

        return statements;
    }

    /**
     * 获取参数语句
     * @param param
     * @param returnTypeName
     * @param msgField
     * @return
     */
    private JCTree.JCStatement getParamStatements(JCTree.JCVariableDecl param, String returnTypeName, JCTree.JCIdent msgField) {
        CheckData checkData = getCheckData(param, returnTypeName);
        if (null == checkData) {
            return null;
        }
        Symbol.VarSymbol symbol = param.sym;
        String paramTypeName = CTreeUtil.getOriginalTypeName(symbol);
        byte classType = getClassType(paramTypeName);

        JCTree.JCIdent self = treeMaker.Ident(param.name);
        String varName = param.name.toString();
        ActionData actionData = checkData.buildActionData();

        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        // 获取基础类型规则
        java.util.List<Regulation> regulations = RegulationBuilder.findBasicRule(symbol, paramTypeName, classType, actionData);

        boolean isEmpty = regulations.isEmpty();
        // 无基础类型规则则检测自定义规则
        if (isEmpty && ruleClass.contains(paramTypeName)) {
            // 将校验结果赋值给临时变量
            JCTree.JCStatement action = checkData.getAction(msgField);
            EqualsRegulation equalsRegulation = new EqualsRegulation(TypeUtil.OBJECT, false);

            statements.append(assignCallback(self, Element.getName(paramTypeName), msgField));
            statements = equalsRegulation.handle(makerContext, statements, varName, msgField, action);

            isEmpty = false;
        }

        Boolean checkNull = RegulationBuilder.checkNotNull(symbol, classType);

        // 非原始类型
        if (null != checkNull) {
            if (checkNull) {
                NullCheckRegulation nullCheckRegulation = new NullCheckRegulation();
                nullCheckRegulation.setActionData(actionData);
                regulations.add(nullCheckRegulation);
                isEmpty = false;
            } else {
                // 无规则不使用非空包装
                if (!isEmpty) {
                    regulations.add(new NotNullWrapperRegulation());
                }
            }
        }

        if (isEmpty) {
            return null;
        }

        for (Regulation regulation : regulations) {
            statements = regulation.handle(makerContext, statements, varName, self, checkData.returnValue);
        }
        return getBlock(statements);
    }

    /**
     * 获取校验信息
     * @param param
     * @param returnTypeName
     * @return
     */
    private CheckData getCheckData(JCTree.JCVariableDecl param, String returnTypeName) {
        Throw throwAttr = CTreeUtil.getAnnotation(param.sym, Throw.class);
        if (null != throwAttr) {
            // 默认异常类
            Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Throw.class.getName());
            String exception = getException(annotationAttr);

            String message = throwAttr.message();
            if (message.isEmpty()) {
                message = Element.MESSAGE + Element.ACTION_INFO;
            } else {
                message += Element.ACTION_INFO;
            }

            return new CheckData(exception, message);
        }

        Return returnAttr = CTreeUtil.getAnnotation(param.sym, Return.class);
        if (null != returnAttr && returnTypeName != null) {
            Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Return.class.getName());
            // 有指定类型
            String type = CTreeUtil.getAnnotationValue(annotationAttr, "type()");

            if (null != type && !type.equals(Object.class.getName())) {
                type = TypeUtil.getWrapperType(type);

                if (isSubClass(type, returnTypeName)) {
                    returnTypeName = type;
                } else {
                    // 类型不一致
                    messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] " + type + " cannot convert to " + returnTypeName);
                }
            }

            JCTree.JCStatement statement = getReturn(returnTypeName, returnAttr.value());
            if (statement == null) {
                System.exit(1);
            }
            return new CheckData(statement);
        }

        return null;
    }

    /**
     * 获取返回对象语句
     * @param classType
     * @return
     */
    private JCTree.JCStatement getReturn(String classType, String[] values) {
        JCTree.JCExpression returnValue;
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(classType);
        boolean errorType;
        if (null == classSymbol) {
            // primitive type
            errorType = false;
        } else {
            errorType = CTreeUtil.isAbsOrInter(classSymbol.flags());
        }

        if (Element.isReturnNull(values)) {
            // 返回空对象
            if (TypeUtil.isPrimitive(classType)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot return <nulltype> value to " + classType);
            }
            returnValue = makerContext.nullNode;
            return treeMaker.Return(returnValue);
        }

        // 无法创建抽象类和接口
        if (errorType) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot find constructor from abstract or interface, return type is " + classType);
            return null;
        }

        int length = values.length;
        if (length == 0) {
            // 返回空构造方法
            return treeMaker.Return(getEmptyType(classType));
        } else if (length == 1) {
            String value = values[0];
            if (value.isEmpty()) {
                // 返回空构造方法
                return treeMaker.Return(getEmptyType(classType));
            }

            // 基本类型
            TypeTag baseType = TypeUtil.getBaseType(classType);
            if (null != baseType) {
                Object tagValue = CTreeUtil.getValue(baseType, value);
                if (null == tagValue) {
                    // 数据有误
                    messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot convert " + value + " to " + classType);
                }
                returnValue = CTreeUtil.newElement(treeMaker, baseType, tagValue);
                return treeMaker.Return(returnValue);
            }
        }

        // 类构造方法
        JCTree.JCExpression returnType = makerContext.findClass(classType);
        List<JCTree.JCExpression> paramType = getParamType(classSymbol, values);
        if (null == paramType) {
            // 无匹配的构造方法
            messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot find match param constructor with " + Arrays.toString(values));
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
                messager.printMessage(Diagnostic.Kind.ERROR, "[Exception Error] " + exception + " must be provide a String constructor!");
            }
        }
        return exception;
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

        // Return 表达式
        private JCTree.JCStatement returnValue;

        // Throw 类型
        private String exceptionName;

        // 信息
        private String info;

        CheckData(JCTree.JCStatement returnValue) {
            this.returnValue = returnValue;
        }

        CheckData(String exceptionName, String info) {
            this.exceptionName = exceptionName;
            this.info = info;
        }

        ActionData buildActionData() {
            if (null != returnValue) {
                return null;
            }
            return new ActionData(BaseRegulation.THROW, exceptionName, info);
        }

        private JCTree.JCStatement getAction(JCTree.JCIdent msgField) {
            if (null != returnValue) {
                return returnValue;
            }

            JCTree.JCExpression message = concatStatement(msgField, info);
            return newMsgThrow(message, exceptionName);
        }
    }
}
