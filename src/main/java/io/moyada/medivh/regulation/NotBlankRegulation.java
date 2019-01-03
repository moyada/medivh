package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 非空白字符串校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NotBlankRegulation extends BaseRegulation implements Regulation {

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();

        // 调用方法进行校验
        JCTree.JCExpression aClass = makerContext.findClass(Element.BLANK_METHOD[0]);
        JCTree.JCMethodInvocation isBlank = makerContext.getMethod(aClass, Element.BLANK_METHOD[1], List.of(self));

        // 返回值为 true 执行动作语句
        JCTree.JCExpression condition = CTreeUtil.newExpression(treeMaker, TypeTag.EQ, isBlank, makerContext.trueNode);
        JCTree.JCIf anIf = treeMaker.If(condition, action, null);
        return anIf;
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + Element.BLANK_INFO;
    }
}
