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

    ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                          String fieldName, JCTree.JCExpression self, JCTree.JCExpression rival,
                                          JCTree.JCStatement action);
}
