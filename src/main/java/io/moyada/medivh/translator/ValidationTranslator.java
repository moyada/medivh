package io.moyada.medivh.translator;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.regulation.*;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.Element;
import io.moyada.medivh.util.TypeTag;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验方法生成器
 * 对定义 @link{Rule} 的类增加 ValidationTranslator.METHOD_NAME 的校验方法
 * @author xueyikang
 * @since 1.0
 **/
public class ValidationTranslator extends BaseTranslator {

    public ValidationTranslator(Context context, Messager messager) {
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

                Symbol.VarSymbol symbol = jcVariableDecl.sym;
                String className = CTreeUtil.getOriginalTypeName(symbol);
                // 获取参数规则
                BaseRegulation regulation = RegulationHelper.build(symbol, className, isCollection(className));
                if (null == regulation) {
                    continue;
                }

                validationRule.put(treeMaker.Ident(jcVariableDecl.name), regulation);
            }
        }
        if (validationRule.isEmpty()) {
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Create " + Element.METHOD_NAME + " method in " + jcClassDecl.sym.className());

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
        BaseRegulation regulation;

        boolean nullcheck;
        JCTree.JCReturn returnStatement = treeMaker.Return(nullNode);
        for (Map.Entry<JCTree.JCIdent, BaseRegulation> entry : validationRule.entrySet()) {
            key = entry.getKey();
            regulation = entry.getValue();

            // 非原始类型且不可非空
            nullcheck = !regulation.isPrimitive() && !regulation.isNullable();
            if (nullcheck) {
                addNotNullCheck(statements, key);
            }

            if (regulation instanceof NumberRegulation) {
                addRangeCheck(statements, key, (NumberRegulation) regulation, nullcheck);
            } else if (regulation instanceof SizeRegulation) {
                addSizeCheck(statements, key, (SizeRegulation) regulation, nullcheck);
            } else if (regulation instanceof EqualsRegulation) {
                addEqualsCheck(statements, key, (EqualsRegulation) regulation, nullcheck);
            }
        }

        // 校验通过返回 null
        statements.append(returnStatement);

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
     * @param regulation
     * @param nullcheck
     */
    private void addSizeCheck(ListBuffer<JCTree.JCStatement> statements,
                              JCTree.JCIdent field, SizeRegulation regulation, boolean nullcheck) {
        String fileName = field.name.toString();

        // 获取大小信息
        JCTree.JCExpression getLength;
        String mode;
        if (regulation.isStr()) {
            getLength = execMethod(getMethod(field, "length", CTreeUtil.emptyParam())).getExpression();
            mode = ".length()";
        } else if (regulation.isArr()) {
            getLength = getField(field, "length");
            mode = ".length";
        } else {
            getLength = execMethod(getMethod(field, "size", CTreeUtil.emptyParam())).getExpression();
            mode = ".size()";
        }

        JCTree.JCIf expression = null;

        Integer min = regulation.getMin();
        Integer max = regulation.getMax();
        if (null != min && null != max && min.intValue() == max.intValue()) {
            // equals
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

            // 创建对比语句
            JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, getLength, minField);
            expression = treeMaker.If(condition,
                    treeMaker.Return(createStr(fileName + mode + " cannot equals " + min)), expression);
        } else {
            // min logic
            if (null != min) {
                JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

                // 创建对比语句
                JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, getLength, minField);
                expression = treeMaker.If(condition,
                        treeMaker.Return(createStr(fileName + mode + " less than " + min)), expression);
            }

            // max logic
            if (null != max) {
                JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, max);

                // 创建对比语句
                JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, getLength, minField);
                expression = treeMaker.If(condition,
                        treeMaker.Return(createStr(fileName + mode + " great than " + max)), expression);
            }
        }

        // 没经过判空校验
        if (!nullcheck) {
            JCTree.JCExpression notnull = CTreeUtil.newExpression(treeMaker, TypeTag.NE, field, nullNode);
            expression = treeMaker.If(notnull, expression, null);
        }

        statements.append(expression);
    }

    /**
     * 增加长度校验
     * @param statements
     * @param field
     * @param regulation
     * @param nullcheck
     */
    private void addEqualsCheck(ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCIdent field, EqualsRegulation regulation, boolean nullcheck) {
        String name = field.name.toString();

        TypeTag typeTag = regulation.getTypeTag();

        Object value = regulation.getValue();
        JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, typeTag, value);
        JCTree.JCExpression minCondition = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, field, minField);
        JCTree.JCIf expression = treeMaker.If(minCondition, treeMaker.Return(createStr(name + " less than " + value)), null);

        // 没做过判空并且非原始类型
        if (!nullcheck && !regulation.isPrimitive()) {
            JCTree.JCExpression notnull = CTreeUtil.newExpression(treeMaker, TypeTag.NE, field, nullNode);
            expression = treeMaker.If(notnull, expression, null);
        }

        statements.append(expression);
    }

    /**
     * 增加范围校验
     * @param statements
     * @param field
     * @param regulation
     * @param nullcheck
     */
    private void addRangeCheck(ListBuffer<JCTree.JCStatement> statements,
                               JCTree.JCIdent field, NumberRegulation regulation, boolean nullcheck) {
        String name = field.name.toString();

        JCTree.JCIf expression = null;

        TypeTag typeTag = regulation.getTypeTag();
        // min logic
        Object min = regulation.getMin();
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, typeTag, min);
            JCTree.JCExpression minCondition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, field, minField);
            expression = treeMaker.If(minCondition, treeMaker.Return(createStr(name + " less than " + min)), expression);
        }

        // max logic
        Object max = regulation.getMax();
        if (null != max) {
            JCTree.JCLiteral maxField = CTreeUtil.newElement(treeMaker, typeTag, max);
            JCTree.JCExpression maxCondition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, field, maxField);
            expression = treeMaker.If(maxCondition, treeMaker.Return(createStr(name + " great than " + max)), expression);
        }

        // 没做过判空并且非原始类型
        if (!nullcheck && !regulation.isPrimitive()) {
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
                CTreeUtil.getName(namesInstance, Element.METHOD_NAME),
                findClass(String.class.getName()),
                param, var, thrown,
                body, null);
    }
}
