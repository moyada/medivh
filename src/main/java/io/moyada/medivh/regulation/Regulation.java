package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;

/**
 * 规则处理器
 * @author xueyikang
 * @since 1.0
 **/
public interface Regulation {

    /**
     * 处理规则事件
     * @param makerContext
     * @param statements
     * @param fieldName
     * @param self
     * @param action
     * @return
     */
    ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                          String fieldName, JCTree.JCExpression self, JCTree.JCStatement action);
}
