package io.moyada.medivh.support;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.regulation.Regulation;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 规则处理器
 * @author xueyikang
 * @since 1.3.0
 **/
public class RegulationExecutor {

    private static ExpressionMaker maker;

    private ListBuffer<JCTree.JCStatement> statements;

    private java.util.List<Regulation> regulations;

    private JCTree.JCStatement action;

    static void init(ExpressionMaker maker) {
        if (null == maker) {
            return;
        }
        RegulationExecutor.maker = maker;
    }

    private RegulationExecutor(java.util.List<Regulation> regulations) {
        this.regulations = regulations;
    }

    public static RegulationExecutor newExecutor(java.util.List<Regulation> regulations) {
        return new RegulationExecutor(regulations);
    }

    public RegulationExecutor setStatement(ListBuffer<JCTree.JCStatement> statements) {
        this.statements = statements;
        return this;
    }

    public RegulationExecutor setAction(JCTree.JCStatement action) {
        this.action = action;
        return this;
    }

    public ListBuffer<JCTree.JCStatement> execute(JCTree.JCExpression self, String fieldName) {
        if (null == statements) {
            statements = CTreeUtil.newStatement();
        }

        for (Regulation regulation : regulations) {
            statements = regulation.handle(maker, statements, fieldName, self, action);
        }
        return statements;
    }
}
