package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ExpressionMaker;

/**
 * 规则处理器
 * @author xueyikang
 * @since 1.0
 **/
public interface Regulation {

    /**
     * 规则处理
     * @param expressionMaker 语句构造器
     * @param statements 语句链
     * @param fieldName 元素名称
     * @param self 处理元素
     * @param action 执行事件
     * @return 处理后语句链
     */
    ListBuffer<JCTree.JCStatement> handle(ExpressionMaker expressionMaker, ListBuffer<JCTree.JCStatement> statements,
                                          String fieldName, JCTree.JCExpression self, JCTree.JCStatement action);
}
