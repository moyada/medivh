package cn.moyada.method.validator.translator;

import cn.moyada.method.validator.regulation.BaseRegulation;
import cn.moyada.method.validator.regulation.NumberRegulation;
import cn.moyada.method.validator.regulation.LengthRegulation;
import cn.moyada.method.validator.util.CTreeUtil;
import cn.moyada.method.validator.util.RegulationHelper;
import cn.moyada.method.validator.util.TypeTag;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验方法生成器
 * @author xueyikang
 * @since 1.0
 **/
public class VerificationTranslator extends BaseTranslator {

    // 方法名称
    final static String METHOD_NAME = "invalid0";

    public VerificationTranslator(Context context, Messager messager) {
        super(context, messager);
    }

    /**
     * 扫描类节点
     * @param jcClassDecl
     */
    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        // 过滤接口
        if ((jcClassDecl.mods.flags & Flags.INTERFACE) != 0) {
            return;
        }

        // 解析所有参数规则
        Map<JCTree.JCIdent, BaseRegulation> validationRule = new HashMap<JCTree.JCIdent, BaseRegulation>();

        for (JCTree var : jcClassDecl.defs) {
            // 过滤变量以外
            if (var.getKind().equals(Tree.Kind.VARIABLE)) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) var;
                // 排除枚举
                if ((jcVariableDecl.mods.flags & Flags.ENUM) != 0) {
                    continue;
                }

                // 获取参数规则
                BaseRegulation rule = RegulationHelper.getRule(jcVariableDecl);
                if (null == rule) {
                    continue;
                }

                validationRule.put(treeMaker.Ident(jcVariableDecl.name), rule);
            }
        }
        if (validationRule.isEmpty()) {
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Init " + METHOD_NAME + " method for " + jcClassDecl.sym.className());

        JCTree.JCBlock body = createBody(validationRule);
        JCTree.JCMethodDecl method = createMethod(body);
        // 刷新类信息
        jcClassDecl.defs = jcClassDecl.defs.append(method);
        this.result = jcClassDecl;
    }

    /**
     * 创建代码块
     * @param validationRule
     * @return
     */
    private JCTree.JCBlock createBody(Map<JCTree.JCIdent, BaseRegulation> validationRule) {
        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();

        JCTree.JCIdent key;
        BaseRegulation validation;

        boolean nullcheck;
        JCTree.JCReturn returnStatement = treeMaker.Return(nullNode);
        for (Map.Entry<JCTree.JCIdent, BaseRegulation> entry : validationRule.entrySet()) {
            key = entry.getKey();
            validation = entry.getValue();

            // 非原始类型且不可非空
            nullcheck = !validation.isPrimitive() && !validation.isNullable();
            if (nullcheck) {
                addNotNullCheck(statements, key);
            }
            addRangeCheck(statements, key, validation, nullcheck);
            addLengthCheck(statements, key, validation, nullcheck);
        }

        // 校验通过返回 null
        statements.add(returnStatement);

        return getBlock(statements);
    }

    /**
     * 增加非空校验
     * @param statements
     * @param field
     */
    private void addNotNullCheck(ListBuffer<JCTree.JCStatement> statements, JCTree.JCIdent field) {
        JCTree.JCExpression nullCheck = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, field, nullNode);
        statements.append(treeMaker.If(nullCheck, treeMaker.Return(createStr(field.name.toString() + " is null")), null));
    }

    /**
     * 增加长度校验
     * @param statements
     * @param field
     * @param validation
     * @param nullcheck
     */
    private void addLengthCheck(ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCIdent field, BaseRegulation validation, boolean nullcheck) {

        if (!(validation instanceof LengthRegulation)) {
            return;
        }
        LengthRegulation lengthValidation = (LengthRegulation) validation;
        int length = lengthValidation.getLength();
        JCTree.JCLiteral lenField = CTreeUtil.newElement(treeMaker, TypeTag.INT, length);

        // 获取长度信息
        JCTree.JCExpression getLength;
        JCTree.JCLiteral message;
        if (lengthValidation.isStr()) {
            getLength = execMethod(getMethod(field, "length", CTreeUtil.emptyParam())).getExpression();
            message = createStr(field.name.toString() + ".length() great than " + length);
        } else {
            getLength = getField(field, "length");
            message = createStr(field.name.toString() + ".length great than " + length);
        }

        // 创建对比语句
        JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, getLength, lenField);
        JCTree.JCIf expression = treeMaker.If(condition, treeMaker.Return(message), null);

        // 没经过判空校验
        if (!nullcheck) {
            JCTree.JCExpression notnull = CTreeUtil.newExpression(treeMaker, TypeTag.NE, field, nullNode);
            expression = treeMaker.If(notnull, expression, null);
        }
        statements.append(expression);
    }

    /**
     * 增加范围校验
     * @param statements
     * @param field
     * @param validation
     * @param nullcheck
     */
    private void addRangeCheck(ListBuffer<JCTree.JCStatement> statements,
                               JCTree.JCIdent field, BaseRegulation validation, boolean nullcheck) {

        if (!(validation instanceof NumberRegulation)) {
            return;
        }
        NumberRegulation numberValidation = (NumberRegulation) validation;

        String name = field.name.toString();

        // min logic
        long min = numberValidation.getMin();
        JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);
        JCTree.JCExpression minCondition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, field, minField);

        // max logic
        long max = numberValidation.getMax();
        JCTree.JCLiteral maxField = CTreeUtil.newElement(treeMaker, TypeTag.INT, max);
        JCTree.JCExpression maxCondition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, field, maxField);

        // 创建判断语句
        JCTree.JCIf expression = treeMaker.If(maxCondition, treeMaker.Return(createStr(name + " great than " + max)), null);
        expression = treeMaker.If(minCondition, treeMaker.Return(createStr(name + " less than " + min)), expression);

        // 没做过判空并且非原始类型
        if (!nullcheck && !validation.isPrimitive()) {
            JCTree.JCExpression notnull = CTreeUtil.newExpression(treeMaker, TypeTag.NE, field, nullNode);
            expression = treeMaker.If(notnull, expression, null);
        }

        statements.append(expression);
    }

    /**
     * 获取 java.lang.String 属性
     * @param value
     * @return
     */
    private JCTree.JCLiteral createStr(String value) {
        return CTreeUtil.newElement(treeMaker, TypeTag.CLASS, value);
    }

    /**
     * 创建校验方法
     * @param body
     * @return
     */
    private JCTree.JCMethodDecl createMethod(JCTree.JCBlock body) {
        List<JCTree.JCTypeParameter> param = List.nil();
        List<JCTree.JCVariableDecl> var = List.nil();
        List<JCTree.JCExpression> thrown = List.nil();
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(METHOD_NAME),
                findClass(String.class.getName()),
                param, var, thrown,
                body, null);
    }
}
