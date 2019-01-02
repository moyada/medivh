package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.util.TypeUtil;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class SizeRangeRegulation extends BaseRegulation implements Regulation {

    private Integer min;
    private Integer max;

    private byte type;

    public SizeRangeRegulation(Integer min, Integer max, byte type) {
        this.min = min;
        this.max = max;
        this.type = type;
    }

    private boolean isEquals() {
        return null != min && null != max && min.intValue() == max.intValue();
    }

    private boolean isStr() {
        return type == TypeUtil.STRING;
    }

    private boolean isArr() {
        return type == TypeUtil.ARRAY;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCExpression rival, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression getLength;
        if (isStr()) {
            getLength = treeMaker.Exec(makerContext.getMethod(self, "length", CTreeUtil.emptyParam())).getExpression();
        } else if (isArr()) {
            getLength = makerContext.getField(self, "length");
        } else {
            getLength = treeMaker.Exec(makerContext.getMethod(self, "size", CTreeUtil.emptyParam())).getExpression();
        }

        JCTree.JCIf expression = null;

        if (isEquals()) {
            // equals
            JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

            // 创建对比语句
            JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.NE, getLength, minField);

            if (null == action) {
                String msg = info + Element.EQUALS_INFO + " " + min;
                action = createAction(makerContext, msg);
            }
            expression = treeMaker.If(condition, action, expression);
        } else {
            // min logic
            if (null != min) {
                JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, min);

                // 创建对比语句
                JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.LT, getLength, minField);

                String msg = info + Element.LESS_INFO + " " + min;
                JCTree.JCStatement lessAction = getAction(makerContext, action, msg);

                expression = treeMaker.If(condition, lessAction, expression);
            }

            // max logic
            if (null != max) {
                JCTree.JCLiteral minField = CTreeUtil.newElement(treeMaker, TypeTag.INT, max);

                // 创建对比语句
                JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.GT, getLength, minField);

                String msg = info + Element.GREAT_INFO + " " + max;
                JCTree.JCStatement greatAction = getAction(makerContext, action, msg);

                expression = treeMaker.If(condition, greatAction, expression);
            }
        }

        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        String mode;
        if (isStr()) {
            mode = ".length()";
        } else if (isArr()) {
            mode = ".length";
        } else {
            mode = ".size()";
        }

        return fieldName + mode + " ";
    }

    private JCTree.JCStatement getAction(MakerContext makerContext, JCTree.JCStatement action, String info) {
        if (null != action) {
            return action;
        }
        return createAction(makerContext, info);
    }
}
