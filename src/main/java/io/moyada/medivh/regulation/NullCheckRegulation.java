package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.TypeTag;

/**
 * 校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NullCheckRegulation extends BaseRegulation implements Regulation {

    @Override
    public ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCExpression rival,
                                                 JCTree.JCStatement action) {
        if (null == action) {
            action = createAction(makerContext, buildInfo(fieldName));
        }
        JCTree.JCStatement exec = doHandle(makerContext, statements, self, rival, action);
        statements.prepend(exec);
        return statements;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                         JCTree.JCExpression self, JCTree.JCExpression rival,
                         JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();
        JCTree.JCExpression condition;

        condition = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, self, rival);
        return treeMaker.If(condition, action, null);
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + Element.NULL_INFO;
    }
}
