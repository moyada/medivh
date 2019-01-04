package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 空间范围校验规则
 * 使用固定范围，不于外界因素做比较
 * @author xueyikang
 * @since 1.0
 **/
public class SizeRangeRegulation extends TypeRegulation implements Regulation {

    private Integer min;
    private Integer max;

    public SizeRangeRegulation(Integer min, Integer max, byte type) {
        super(type);
        this.min = min;
        this.max = max;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {

        TreeMaker treeMaker = makerContext.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression getLength = getExpr(makerContext, self);

        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

            // 创建对比语句
            JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, getLength, minField);

            JCTree.JCStatement lessAction;
            if (null == info) {
                lessAction = action;
            } else {
                String msg = info + Element.LESS_INFO + " " + min;
                lessAction = createAction(makerContext, msg);
            }

            expression = treeMaker.If(condition, lessAction, expression);
        }

        // max logic
        if (null != max) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, max);

            // 创建对比语句
            JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, getLength, minField);

            JCTree.JCStatement greatAction;
            if (null == info) {
                greatAction = action;
            } else {
                String msg = info + Element.GREAT_INFO + " " + max;
                greatAction = createAction(makerContext, msg);
            }

            expression = treeMaker.If(condition, greatAction, expression);
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + getMode() + " ";
    }
}
