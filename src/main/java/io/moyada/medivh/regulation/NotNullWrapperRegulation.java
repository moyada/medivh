package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.TypeTag;

/**
 * 校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NotNullWrapperRegulation implements Regulation {

    @Override
    public ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCExpression rival,
                                                 JCTree.JCStatement action) {

        TreeMaker treeMaker = makerContext.getTreeMaker();
        JCTree.JCExpression condition;
        condition = CTreeUtil.newExpression(treeMaker, TypeTag.NE, self, rival);
        JCTree.JCIf exec = treeMaker.If(condition, treeMaker.Block(0, statements.toList()), null);

        ListBuffer<JCTree.JCStatement> jcStatements = CTreeUtil.newStatement();
        jcStatements.append(exec);
        return jcStatements;
    }
}
