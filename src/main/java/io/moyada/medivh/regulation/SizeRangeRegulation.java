package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.*;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 空间范围校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class SizeRangeRegulation extends BaseRegulation implements Regulation {

    // 最小值
    private final Integer min;
    // 最大值
    private final Integer max;

    // 类型数据获取支持
    private final TypeFetchSupport typeFetchSupport;

    // 临时变量支持
    private final LocalVarSupport localVarSupport;

    public SizeRangeRegulation(Integer min, Integer max, byte type) {
        this.min = min;
        this.max = max;
        this.localVarSupport = new LocalVarSupport(TypeTag.INT);
        this.typeFetchSupport = new TypeFetchSupport(type);
    }

    @Override
    JCTree.JCStatement doHandle(ExpressionMaker expressionMaker, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {

        TreeMaker treeMaker = expressionMaker.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression getLength;

        if (null != min && null != max) {
            getLength = typeFetchSupport.getExpr(expressionMaker, self);
            getLength = localVarSupport.getValue(expressionMaker, statements, getLength);
        } else {
            getLength = typeFetchSupport.getExpr(expressionMaker, self);
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
                String msg = info + ElementOptions.LESS_INFO + " " + min;
                lessAction = createAction(expressionMaker, msg);
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
                String msg = info + ElementOptions.GREAT_INFO + " " + max;
                greatAction = createAction(expressionMaker, msg);
            }

            expression = treeMaker.If(condition, greatAction, expression);
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + typeFetchSupport.getMode() + " ";
    }
}
