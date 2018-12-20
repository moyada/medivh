package cn.moyada.function.validator.translator;

import cn.moyada.function.validator.core.BaseValidation;
import cn.moyada.function.validator.core.NumberValidation;
import cn.moyada.function.validator.util.RuleHelper;
import cn.moyada.function.validator.core.StringValidation;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验方法生成器
 * @author xueyikang
 * @since 1.0
 **/
public class ValidatorTranslator extends BaseTranslator {

    // 方法名称
    final static String METHOD_NAME = "_isInvalid";

    public ValidatorTranslator(Context context) {
        super(context);
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
        Map<JCTree.JCIdent, BaseValidation> validationRule = new HashMap<>();

        for (JCTree var : jcClassDecl.defs) {
            // 过滤变量以外
            if (var.getKind().equals(Tree.Kind.VARIABLE)) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) var;
                // 排除枚举
                if ((jcVariableDecl.mods.flags & Flags.ENUM) != 0) {
                    continue;
                }

                // 获取参数规则
                BaseValidation rule = RuleHelper.getRule(jcVariableDecl);
                if (null == rule) {
                    continue;
                }

                validationRule.put(treeMaker.Ident(jcVariableDecl.name), rule);
            }
        }

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
    private JCTree.JCBlock createBody(Map<JCTree.JCIdent, BaseValidation> validationRule) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        JCTree.JCIdent key;
        BaseValidation validation;

        boolean nullcheck;
        JCTree.JCReturn returnStatement = treeMaker.Return(nullNode);
        for (Map.Entry<JCTree.JCIdent, BaseValidation> entry : validationRule.entrySet()) {
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
        JCTree.JCExpression nullCheck = treeMaker.Binary(JCTree.Tag.EQ, field, nullNode);
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
                                JCTree.JCIdent field, BaseValidation validation, boolean nullcheck) {

        if (!(validation instanceof StringValidation)) {
            return;
        }
        StringValidation stringValidation = (StringValidation) validation;

        int length = stringValidation.getLength();
        JCTree.JCLiteral lenField = treeMaker.Literal(TypeTag.INT, length);

        // 调用 length()
        JCTree.JCExpressionStatement getLength = execMethod(getMethod(field, "length", List.nil()));

        JCTree.JCExpression condition = treeMaker.Binary(JCTree.Tag.GT, getLength.getExpression(), lenField);
        JCTree.JCIf expression = treeMaker.If(condition, treeMaker.Return(createStr(field.name.toString() + ".length() great than " + length)), null);

        // 没经过判空校验
        if (!nullcheck) {
            JCTree.JCExpression notnull = treeMaker.Binary(JCTree.Tag.NE, field, nullNode);
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
                               JCTree.JCIdent field, BaseValidation validation, boolean nullcheck) {

        if (!(validation instanceof NumberValidation)) {
            return;
        }
        NumberValidation numberValidation = (NumberValidation) validation;


        String name = field.name.toString();

        // min logic
        long min = numberValidation.getMin();
        JCTree.JCLiteral minField = treeMaker.Literal(TypeTag.INT, min);
        JCTree.JCBinary minCondition = treeMaker.Binary(JCTree.Tag.LT, field, minField);

        // max logic
        long max = numberValidation.getMax();
        JCTree.JCLiteral maxField = treeMaker.Literal(TypeTag.INT, max);
        JCTree.JCBinary maxCondition = treeMaker.Binary(JCTree.Tag.GT, field, maxField);

        JCTree.JCIf expression = treeMaker.If(maxCondition, treeMaker.Return(createStr(name + " great than " + max)), null);
        expression = treeMaker.If(minCondition, treeMaker.Return(createStr(name + " less than " + min)), expression);

        // 没做过判空并且非原始类型
        if (!nullcheck && !validation.isPrimitive()) {
            JCTree.JCExpression notnull = treeMaker.Binary(JCTree.Tag.NE, field, nullNode);
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
        return treeMaker.Literal(TypeTag.CLASS, value);
    }

    /**
     * 创建校验方法
     * @param body
     * @return
     */
    private JCTree.JCMethodDecl createMethod(JCTree.JCBlock body) {
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(METHOD_NAME),
                findClass(String.class.getName()),
                List.nil(), List.nil(), List.nil(),
                body, null);
    }
}
