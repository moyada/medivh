package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.SyntaxTreeMaker;

/**
 * 临时变量存储，用于保存方法的调用数据
 * @author xueyikang
 * @since 1.2.2
 **/
public class LocalVariableRegulation implements Regulation {

    // 临时变量元素
    private final JCTree.JCVariableDecl localVar;

    public LocalVariableRegulation(JCTree.JCVariableDecl localVar) {
        this.localVar = localVar;
    }

    @Override
    public ListBuffer<JCTree.JCStatement> handle(SyntaxTreeMaker syntaxTreeMaker, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCStatement action) {
        statements.prepend(localVar);
        return statements;
    }
}
