package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.TypeTag;

/**
 * 数字范围校验规则
 * 使用固定数值，不于外界因素做比较
 * @author xueyikang
 * @since 1.0
 **/
public class NumberRegulation extends BaseRegulation implements Regulation {

    // 数据类型
    private TypeTag typeTag;

    // 最小值
    private Object min;

    // 最大值
    private Object max;

    public NumberRegulation(TypeTag typeTag, Object min, Object max) {
        this.typeTag = typeTag;
        this.min = min;
        this.max = max;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();
        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, typeTag, min);
            JCTree.JCExpression minCondition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, self, minField);

            String msg = info + " " + Element.LESS_INFO + " " + min;
            JCTree.JCStatement lessAction = createActionIfNotExist(action, makerContext, msg);

            expression = treeMaker.If(minCondition, lessAction, expression);
        }

        // max logic
        if (null != max) {
            JCTree.JCLiteral maxField = CTreeUtil.newElement(treeMaker, typeTag, max);
            JCTree.JCExpression maxCondition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, self, maxField);

            String msg = info + " " + Element.GREAT_INFO + " " + max;
            JCTree.JCStatement greatAction = createActionIfNotExist(action, makerContext, msg);

            expression = treeMaker.If(maxCondition, greatAction, expression);
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName;
    }
}
