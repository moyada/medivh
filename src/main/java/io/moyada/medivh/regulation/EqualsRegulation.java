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
public class EqualsRegulation extends BaseRegulation implements Regulation {

    private TypeTag typeTag;

    private Object value;

    private boolean equals;

    public EqualsRegulation(boolean equals) {
        this.equals = equals;
    }

    public EqualsRegulation(TypeTag typeTag, Object value, boolean equals) {
        this.typeTag = typeTag;
        this.value = value;
        this.equals = equals;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCExpression rival, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();
        if (null != value) {
            rival = CTreeUtil.newElement(treeMaker, typeTag, value);
        }

        TypeTag typeTag;
        if (equals) {
            typeTag = TypeTag.EQ;
        } else {
            typeTag = TypeTag.NE;
        }
        JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, typeTag, self, rival);
        JCTree.JCIf expression = treeMaker.If(condition, action, null);
        return expression;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + Element.EQUALS_INFO + " " + value;
    }
}
