package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.LocalVarSupport;
import io.moyada.medivh.support.ExpressionMaker;
import io.moyada.medivh.support.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 数字范围校验规则
 * 使用固定数值，不于外界因素做比较
 * @author xueyikang
 * @since 1.0
 **/
public class NumberRegulation extends BaseRegulation implements Regulation {

    // 数据类型
    private final TypeTag typeTag;

    // 最小值
    private final Object min;

    // 最大值
    private final Object max;

    // 临时变量支持
    private final LocalVarSupport localVarSupport;

    public NumberRegulation(TypeTag typeTag, Object min, Object max) {
        this.typeTag = typeTag;
        this.min = min;
        this.max = max;

        this.localVarSupport = new LocalVarSupport(typeTag);
    }

    @Override
    JCTree.JCStatement doHandle(ExpressionMaker expressionMaker, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = expressionMaker.getTreeMaker();

        if (null != min && null != max) {
            self = localVarSupport.getValue(expressionMaker, statements, self);
        }

        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, typeTag, min);
            JCTree.JCExpression minCondition = CTreeUtil.newBinary(treeMaker, TypeTag.LT, self, minField);

            JCTree.JCStatement lessAction;
            if (null == info) {
                lessAction = action;
            } else {
                String msg = info + ElementOptions.LESS_INFO + " " + min;
                lessAction = createAction(expressionMaker, msg);
            }

            expression = treeMaker.If(minCondition, lessAction, expression);
        }

        // max logic
        if (null != max) {
            JCTree.JCLiteral maxField = CTreeUtil.newElement(treeMaker, typeTag, max);
            JCTree.JCExpression maxCondition = CTreeUtil.newBinary(treeMaker, TypeTag.GT, self, maxField);

            JCTree.JCStatement greatAction;
            if (null == info) {
                greatAction = action;
            } else {
                String msg = info + ElementOptions.GREAT_INFO + " " + max;
                greatAction = createAction(expressionMaker, msg);
            }

            expression = treeMaker.If(maxCondition, greatAction, expression);
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " ";
    }
}
