package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.util.StringUtil;
import io.moyada.medivh.core.TypeTag;

/**
 * 校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NotBlankRegulation extends BaseRegulation implements Regulation {

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCExpression rival, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();

        JCTree.JCExpression aClass = makerContext.findClass(StringUtil.class.getName());
        JCTree.JCMethodInvocation isBlank = makerContext.getMethod(aClass, "isBlank", List.of(self));

        JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, isBlank, makerContext.trueNode);
        JCTree.JCIf anIf = treeMaker.If(condition, action, null);
        return anIf;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + Element.BLANK_INFO;
    }
}
