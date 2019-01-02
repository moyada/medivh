package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.regulation.MethodRegulationContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.SystemUtil;
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
        JCTree.JCVariableDecl msg = newVar(SystemUtil.getTmpVar(methodDecl.sym.getAnnotation(Variable.class)),
                0L, String.class.getName(), null);
        JCTree.JCIdent ident = treeMaker.Ident(msg.name);

        // 建立校验逻辑
        ListBuffer<JCTree.JCStatement> statements = buildLogic(methodDecl.params, returnTypeName, ident);
        if (statements.isEmpty()) {
            return;
        }

        statements.prepend(msg);
        List<JCTree.JCStatement> oldStatements = methodDecl.body.stats;
        int size = oldStatements.size();
        for (int i = 0; i < size; i++) {
            statements.append(oldStatements.get(i));
        }

        // 获取代码块
        JCTree.JCBlock body = getBlock(statements);
        methodDecl.body = body;
        this.result = newMethod(methodDecl, body);
    }

    /**
     *
     * @param params
     * @param returnTypeName
     * @param msgField
     * @return
     */
    public ListBuffer<JCTree.JCStatement> buildLogic(List<JCTree.JCVariableDecl> params,
                                                     String returnTypeName, JCTree.JCIdent msgField) {

        // 建立校验逻辑
        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        MethodRegulationContext methodRegulationContext = new MethodRegulationContext(makerContext);

        for (JCTree.JCVariableDecl param : params) {
            JCTree.JCStatement paramStatements =
                    getParamStatements(methodRegulationContext, param, returnTypeName, msgField);
            if (null == paramStatements) {
                continue;
            }
            statements.append(paramStatements);
        }

        return statements;
    }

    /**
     * 获取参数语句
     * @param methodRegulationContext
     * @param param
     * @param returnTypeName
     * @param msgField
     * @return
     */
    private JCTree.JCStatement getParamStatements(MethodRegulationContext methodRegulationContext,
                                                  JCTree.JCVariableDecl param, String returnTypeName,
                                                  JCTree.JCIdent msgField) {
        CheckData checkData = getCheckData(param, returnTypeName);
        if (null == checkData) {
            return null;
        }
        Symbol.VarSymbol symbol = param.sym;

        String paramTypeName = CTreeUtil.getOriginalTypeName(symbol);
        // not support primitive type
        if (TypeUtil.isPrimitive(paramTypeName)) {
            return null;
        }

        byte type = getClassType(paramTypeName);
        if (type == TypeUtil.PRIMITIVE) {
            return null;
        }

        boolean contains = ruleClass.contains(paramTypeName);
        String paramName = param.name.toString();
        JCTree.JCIdent paramIdent = treeMaker.Ident(param.name);

        methodRegulationContext.newStatement(paramName);

        // 是否可以为空
        Nullable nullable = symbol.getAnnotation(Nullable.class);
        if (null == nullable) {
            JCTree.JCStatement action = checkData.getAction(paramName + " is null");
            methodRegulationContext.checkNotNull(paramIdent, action);
        } else {
            if (!contains && type == TypeUtil.OBJECT) {
                return null;
            }
        }

        // 校验结果判定
        JCTree.JCStatement action;

        switch (type) {
            case TypeUtil.STRING:
                NotBlank notBlank = symbol.getAnnotation(NotBlank.class);
                // 空白字符串校验
                if (null != notBlank) {
                    action = checkData.getAction(paramName + " " + Element.BLANK_INFO);
                    methodRegulationContext.checkNotBlank(paramIdent, action);
                }
            case TypeUtil.ARRAY:
            case TypeUtil.COLLECTION:
                action = checkData.getAction(paramName + ".size invalid");
                methodRegulationContext.checkSize(symbol, type, paramIdent, action);
                break;
            default:
                if (!contains) {
                    break;
                }
                action = checkData.getAction(msgField);
                // 将校验结果赋值给临时变量
                JCTree.JCExpressionStatement assign = assignCallback(paramIdent, msgField);
                methodRegulationContext.checkValid(assign, msgField, action);
        }

        // 需要非空包装
        if (null != nullable) {
            methodRegulationContext.wrapper(paramIdent, null);
        }

        return methodRegulationContext.create();
    }

    /**
     * 获取校验信息
     * @param param
     * @param returnTypeName
     * @return
     */
    private CheckData getCheckData(JCTree.JCVariableDecl param, String returnTypeName) {
        Throw throwAttr = param.sym.getAnnotation(Throw.class);
        if (null != throwAttr) {
            // 默认异常类
            Attribute.Compound annotationAttr = CTreeUtil.getAnnotationAttr(param.sym.getAnnotationMirrors(), Throw.class.getName());
            String exception = getException(annotationAttr);

            String message = throwAttr.message();
            if (message.equals("")) {
                message = Element.MESSAGE + Element.ACTION_INFO;
            } else {
                message += Element.ACTION_INFO;
            }

            return new CheckData(exception, message);
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
            return new CheckData(statement);
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
            returnValue = makerContext.nullNode;
            return treeMaker.Return(returnValue);
        }

        if (errorType) {
            messager.printMessage(Diagnostic.Kind.ERROR, "cannot find constructor from abstract or interface, return type is " + returnTypeName);
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

        // 类构造方法
        JCTree.JCExpression returnType = makerContext.findClass(returnTypeName);
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

        CheckData(JCTree.JCStatement returnValue) {
            this.returnValue = returnValue;
        }

        CheckData(String exceptionName, String info) {
            this.exceptionName = exceptionName;
            this.info = info;
        }

        private JCTree.JCStatement getAction(String msg) {
            if (null != returnValue) {
                return returnValue;
            }

            JCTree.JCLiteral message = CTreeUtil.newElement(treeMaker, TypeTag.CLASS, info + msg);
            return newMsgThrow(message, exceptionName);
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
