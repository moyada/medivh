package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.Return;
import io.moyada.medivh.annotation.Throw;
import io.moyada.medivh.support.*;
import io.moyada.medivh.regulation.*;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.CheckUtil;
import io.moyada.medivh.util.TypeUtil;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 方法校验逻辑生成器
 * 对定义的注解的方法增加入参合法性校验
 * @author xueyikang
 * @since 1.0
 **/
public class ValidationTranslator extends BaseTranslator {

    // 可继承元素校验信息
    private final Map<Symbol, CheckData> classCheck = new HashMap<Symbol, CheckData>();

    public ValidationTranslator(ExpressionMaker expressionMaker, Messager messager) {
        super(expressionMaker, messager);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
        super.visitMethodDef(methodDecl);

        Symbol.MethodSymbol methodSymbol = methodDecl.sym;
        if (CheckUtil.isExclusive(methodSymbol)) {
            return;
        }

        String fullName = CTreeUtil.getFullName(methodSymbol);
        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Build Validation logic for " + fullName + "()");

        // 返回类型
        String returnTypeName = CTreeUtil.getReturnTypeName(methodDecl);

        // 创建临时变量提取引用
        JCTree.JCVariableDecl msg = expressionMaker.newLocalVar(CheckUtil.getTmpVar(methodSymbol), String.class.getName(), null);
        JCTree.JCIdent ident = treeMaker.Ident(msg.name);

        // 方法域校验信息
        CheckData defaultCheck = getDefaultCheckData(methodSymbol, returnTypeName);

        ListBuffer<JCTree.JCStatement> statements = buildLogic(defaultCheck, methodDecl.params, returnTypeName, ident);
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

        JCTree.JCBlock block = getBlock(statements);
        block.setPos(methodDecl.body.getPreferredPosition());
        methodDecl.body = block;
        this.result = methodDecl;
    }

    /**
     * 获取方法默认校验，当方法无校验信息查询类上配置校验
     * @param symbol 元素
     * @param returnTypeName 方法返回类型
     * @return 校验信息
     */
    private CheckData getDefaultCheckData(Symbol symbol, String returnTypeName) {
        CheckData checkData = getCheckData(symbol, returnTypeName);
        if (null != checkData) {
            return checkData;
        }

        Symbol classSymbol = symbol.getEnclosingElement();
        checkData = classCheck.get(classSymbol);
        if (null != checkData) {
            return checkData;
        }
        checkData = getThrow(classSymbol);
        if (null != checkData) {
            classCheck.put(symbol, checkData);
        }
        return checkData;
    }

    /**
     * 获取校验信息
     * 当 {@link Throw} 存在优先返回，否则返回 {@link Return}
     * @param symbol 元素
     * @param returnTypeName 方法返回类型
     * @return 返回匹配校验信息
     */
    private CheckData getCheckData(Symbol symbol, String returnTypeName) {
        CheckData checkData = getThrow(symbol);
        if (null != checkData) {
            return checkData;
        }

        checkData = getReturn(symbol, returnTypeName);
        return checkData;
    }

    /**
     * 获取异常校验信息
     * @param symbol 元素
     * @return 存在标记注解则返回
     */
    private CheckData getThrow(Symbol symbol) {
        Throw throwAttr = CTreeUtil.getAnnotation(symbol, Throw.class);
        if (null != throwAttr) {
            // 默认异常类
            String exception = getException(symbol);

            String message = throwAttr.message();
            if (message.isEmpty()) {
                message = ElementOptions.MESSAGE;
            }
            message += ElementOptions.ACTION_INFO;

            return new CheckData(exception, message);
        }
        return null;
    }

    /**
     * 获取配置异常，但异常构造函数非法则打印错误信息
     * @param symbol 元素
     * @return 异常类名
     */
    private String getException(Symbol symbol) {
        String exception = CTreeUtil.getAnnotationValue(symbol, Throw.class.getName(), "value()");
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
     * 获取方法校验返回信息
     * 当返回设置错误则打印错误信息
     * @param symbol 元素
     * @param returnTypeName 方法返回类型
     * @return 校验信息
     */
    private CheckData getReturn(Symbol symbol, String returnTypeName) {
        Return returnAttr = CTreeUtil.getAnnotation(symbol, Return.class);
        if (null == returnAttr) {
            return null;
        }
        // 无效的返回类型，void
        if (returnTypeName == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot mark Return annotation on void return method.");
            return null;
        }

        // 有指定类型
        String type = CTreeUtil.getAnnotationValue(symbol, Return.class.getName(), "type()");

        // 静态方法
        String staticMethod = returnAttr.staticMethod();

        if (null != type && !type.equals(Object.class.getName())) {
            if (staticMethod.isEmpty()) {
                type = TypeUtil.getWrapperType(type);

                if (isSubClass(type, returnTypeName)) {
                    returnTypeName = type;
                } else {
                    // 类型不一致
                    messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] " + type + " cannot convert to " + returnTypeName);
                }
            } else {
                returnTypeName = type;
            }
        }

        JCTree.JCStatement statement = getReturn(returnTypeName, staticMethod, returnAttr.value());
        if (statement == null) {
            System.exit(1);
        }
        return new CheckData(statement);
    }

    /**
     * 获取返回对象语句
     * 当返回类型无法匹配则打印错误信息
     * @param classType 返回类型
     * @param method 方法名
     * @param values 返回数据
     * @return 返回语句
     */
    private JCTree.JCStatement getReturn(String classType, String method, String[] values) {
        JCTree.JCExpression returnValue;
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(classType);
        boolean errorType;
        if (null == classSymbol) {
            // primitive type
            errorType = false;
        } else {
            errorType = CTreeUtil.isAbsOrInter(classSymbol.flags());
        }

        boolean primitive = TypeUtil.isPrimitive(classType);

        if (CheckUtil.isReturnNull(values)) {
            // 返回空对象
            if (primitive) {
                messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot return <nulltype> value to " + classType);
            }
            returnValue = expressionMaker.nullNode;
            return treeMaker.Return(returnValue);
        }

        if (!method.isEmpty()) {
            List<JCTree.JCExpression> paramType;
            if (values.length == 0) {
                paramType = CTreeUtil.emptyParam();
            } else {
                paramType = getParamType(classSymbol, false, values);
                if (null == paramType) {
                    // 无匹配的静态方法
                    messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot find match param static method from " + classType + " by " + Arrays.toString(values));
                }
            }
            JCTree.JCExpression clazzType = expressionMaker.findClass(classType);
            returnValue = expressionMaker.getMethod(clazzType, method, paramType);
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
        JCTree.JCExpression returnType = expressionMaker.findClass(classType);
        List<JCTree.JCExpression> paramType = getParamType(classSymbol, true, values);
        if (null == paramType) {
            // 无匹配的构造方法
            messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot find match param constructor from " + classType + " by " + Arrays.toString(values));
        }
        returnValue = treeMaker.NewClass(null, CTreeUtil.emptyParam(), returnType, paramType, null);
        return treeMaker.Return(returnValue);
    }

    /**
     * 返回空构造方法语句
     * 当类型为基础类型则打印错误
     * @param returnTypeName 返回类型
     * @return 构造函数语句
     */
    private JCTree.JCExpression getEmptyType(String returnTypeName) {
        if (null != TypeUtil.getBaseType(returnTypeName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Return Error] cannot find return value to " + returnTypeName);
        }
        JCTree.JCExpression returnType = expressionMaker.findClass(returnTypeName);
        return treeMaker.NewClass(null, CTreeUtil.emptyParam(), returnType, CTreeUtil.emptyParam(), null);
    }

    /**
     * 构建校验逻辑
     *
     * @param defaultCheck 默认校验信息
     * @param params 参数
     * @param returnTypeName 方法返回类型
     * @param msgField 临时变量
     * @return 校验语句链
     */
    private ListBuffer<JCTree.JCStatement> buildLogic(CheckData defaultCheck, List<JCTree.JCVariableDecl> params,
                                                     String returnTypeName, JCTree.JCIdent msgField) {

        // 建立校验逻辑
        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        for (JCTree.JCVariableDecl param : params) {
            JCTree.JCStatement paramStatements = getParamStatement(defaultCheck, param, returnTypeName, msgField);
            if (null == paramStatements) {
                continue;
            }
            statements.append(paramStatements);
        }

        return statements;
    }

    /**
     * 获取参数语句
     *
     * @param defaultCheck 默认校验信息
     * @param param 参数
     * @param returnTypeName 方法返回类型
     * @param msgField 临时变量
     * @return 单条参数校验代码块语句
     */
    private JCTree.JCStatement getParamStatement(CheckData defaultCheck, JCTree.JCVariableDecl param, String returnTypeName, JCTree.JCIdent msgField) {
        Symbol.VarSymbol symbol = param.sym;
        if (CheckUtil.isExclusive(symbol)) {
            return null;
        }

        // 字段域校验信息
        CheckData checkData = getCheckData(symbol, returnTypeName);
        if (null == checkData) {
            if (null == defaultCheck) {
                return null;
            }
            checkData = defaultCheck;
        }

        // 获取参数类型
        String paramTypeName = CTreeUtil.getOriginalTypeName(symbol);
        byte classType = getClassType(paramTypeName);

        // 参数标识
        JCTree.JCIdent self = treeMaker.Ident(param.name);
        // 参数名
        String varName = param.name.toString();

        // 获取校验动作
        ActionData actionData = checkData.buildActionData();

        // 获取基础类型规则
        java.util.List<Regulation> regulations = RegulationBuilder.findBasicRule(symbol, paramTypeName, classType, actionData);
        boolean isEmpty = regulations.isEmpty();

        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        // 无基础类型规则则检测自定义规则
        if (isEmpty && CheckUtil.isRegulable(paramTypeName)) {
            // 将校验结果赋值给临时变量
            JCTree.JCStatement action = checkData.getAction(msgField);
            EqualsRegulation equalsRegulation = new EqualsRegulation(TypeUtil.OBJECT, false);

            statements.append(expressionMaker.assignCallback(self, msgField, CheckUtil.getCheckMethod(paramTypeName), CTreeUtil.emptyParam()));
            statements = equalsRegulation.handle(expressionMaker, statements, varName, msgField, action);

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

        // 当 checkData.returnValue 为 null 时使用 actionData 创建校验处理动作
        for (Regulation regulation : regulations) {
            statements = regulation.handle(expressionMaker, statements, varName, self, checkData.returnValue);
        }
        return getBlock(statements);
    }

    /**
     * 检验逻辑
     */
    class CheckData {

        private ActionData actionData;

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

        /**
         * 创建校验处理数据
         * @return 处理数据
         */
        ActionData buildActionData() {
            if (null != returnValue) {
                return null;
            }
            if (null == actionData) {
                actionData = new ActionData(BaseRegulation.THROW, exceptionName, info);
            }
            return actionData;
        }

        /**
         * 获取处理动作语句
         * @param msgField 临时变量
         * @return 处理语句
         */
        private JCTree.JCStatement getAction(JCTree.JCIdent msgField) {
            if (null != returnValue) {
                return returnValue;
            }

            JCTree.JCExpression message = expressionMaker.concatStatement(info, msgField);
            return expressionMaker.newMsgThrow(exceptionName, message);
        }
    }
}
