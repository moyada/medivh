package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.TypeTag;

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

    public NumberRegulation(TypeTag typeTag, Object min, Object max) {
        this.typeTag = typeTag;
        this.min = min;
        this.max = max;
    }

    @Override
    JCTree.JCStatement doHandle(SyntaxTreeMaker syntaxTreeMaker, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = syntaxTreeMaker.getTreeMaker();

        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = syntaxTreeMaker.newElement(typeTag, min);
            JCTree.JCExpression minCondition = syntaxTreeMaker.newBinary(TypeTag.LT, self, minField);

            JCTree.JCStatement lessAction;
            if (null == info) {
                lessAction = action;
            } else {
                String msg = info + ElementOptions.LESS_INFO + " " + min;
                lessAction = createAction(syntaxTreeMaker, msg);
            }

            expression = treeMaker.If(minCondition, lessAction, expression);
        }

        // max logic
        if (null != max) {
            JCTree.JCLiteral maxField = syntaxTreeMaker.newElement(typeTag, max);
            JCTree.JCExpression maxCondition = syntaxTreeMaker.newBinary(TypeTag.GT, self, maxField);

            JCTree.JCStatement greatAction;
            if (null == info) {
                greatAction = action;
            } else {
                String msg = info + ElementOptions.GREAT_INFO + " " + max;
                greatAction = createAction(syntaxTreeMaker, msg);
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
