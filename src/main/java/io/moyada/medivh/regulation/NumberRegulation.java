package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.TypeTag;

/**
 * 数字校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NumberRegulation extends BaseRegulation implements Regulation {

    private TypeTag typeTag;

    private Object min;

    private Object max;

    public NumberRegulation(TypeTag typeTag, Object min, Object max) {
        this.typeTag = typeTag;
        this.min = min;
        this.max = max;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCExpression rival, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();
        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, typeTag, min);
            JCTree.JCExpression minCondition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, self, minField);
            String msg = info + " " + Element.LESS_INFO + " " + min;
            expression = treeMaker.If(minCondition, createAction(makerContext, msg), expression);
        }

        // max logic
        if (null != max) {
            JCTree.JCLiteral maxField = CTreeUtil.newElement(treeMaker, typeTag, max);
            JCTree.JCExpression maxCondition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, self, maxField);
            String msg = info + " " + Element.GREAT_INFO+ " " + max;

            expression = treeMaker.If(maxCondition, createAction(makerContext, msg), expression);
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName;
    }
}
