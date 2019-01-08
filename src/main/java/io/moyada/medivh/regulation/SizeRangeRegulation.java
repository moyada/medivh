package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.*;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 空间范围校验规则
 * 使用固定范围，不于外界因素做比较
 * @author xueyikang
 * @since 1.0
 **/
public class SizeRangeRegulation extends BaseRegulation implements Regulation {

    private final Integer min;
    private final Integer max;

    private TypeGetSupport typeGetSupport;

    private final LocalVarSupport localVarSupport;

    public SizeRangeRegulation(Integer min, Integer max, byte type) {
        this.min = min;
        this.max = max;
        this.localVarSupport = new LocalVarSupport(TypeTag.INT);
        this.typeGetSupport = new TypeGetSupport(type);
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {

        TreeMaker treeMaker = makerContext.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression getLength;

        if (null != min && null != max) {
            getLength = typeGetSupport.getExpr(makerContext, self);
            getLength = localVarSupport.getValue(makerContext, statements, getLength);
        } else {
            getLength = typeGetSupport.getExpr(makerContext, self);
        }

        JCTree.JCIf expression = null;

        // min logic
        if (null != min) {
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

            // 创建对比语句
            JCTree.JCExpression condition = CTreeUtil.newBinary(treeMaker, TypeTag.LT, getLength, minField);

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
            JCTree.JCExpression condition = CTreeUtil.newBinary(treeMaker, TypeTag.GT, getLength, minField);

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
        return fieldName + typeGetSupport.getMode() + " ";
    }
}
