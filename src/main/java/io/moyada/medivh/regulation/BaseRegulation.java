package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;

/**
 * 校验规则
 * @author xueyikang
 * @since 1.0
 **/
public abstract class BaseRegulation implements Regulation {

    protected String info;

    @Override
    public ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCExpression rival,
                                                 JCTree.JCStatement action) {
        if (null == action) {
            info = buildInfo(fieldName);
            action = createAction(makerContext, info);
        }
        JCTree.JCStatement exec = doHandle(makerContext, statements, self, rival, action);
        statements.append(exec);
        return statements;
    }

    abstract JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                  JCTree.JCExpression self, JCTree.JCExpression rival,
                                  JCTree.JCStatement action);

    abstract String buildInfo(String fieldName);

    JCTree.JCStatement createAction(MakerContext makerContext, String info) {
        return makerContext.returnStr(info);
    }
}
