package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.TypeTag;

/**
 * 空校验规则
 * 默认规则，针对对象判空处理，可以使用 {@link io.moyada.medivh.annotation.Nullable} 取消
 * @author xueyikang
 * @since 1.0
 **/
public class NullCheckRegulation extends BaseRegulation implements Regulation {

    @Override
    public ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCStatement action) {
        if (null == action) {
            action = createAction(makerContext, buildInfo(fieldName));
        }
        JCTree.JCStatement exec = doHandle(makerContext, statements, self, action);
        // 将非空处理提前
        statements.prepend(exec);
        return statements;
    }

    @Override
    JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                         JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = makerContext.getTreeMaker();
        // 等于 null 执行动作
        JCTree.JCExpression condition = CTreeUtil.newBinary(treeMaker, TypeTag.EQ, self, makerContext.nullNode);
        return treeMaker.If(condition, action, null);
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + Element.NULL_INFO;
    }
}
