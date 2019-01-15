package io.moyada.medivh.support;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.regulation.Regulation;
import io.moyada.medivh.util.TreeUtil;

/**
 * 规则处理器
 * @author xueyikang
 * @since 1.3.0
 **/
public class RegulationExecutor {

    // 语法创建工具
    private static SyntaxTreeMaker maker;

    // 语句链
    private ListBuffer<JCTree.JCStatement> statements;

    // 规则链
    private java.util.List<Regulation> regulations;

    // 分支动作
    private JCTree.JCStatement action;

    /**
     * 引用语法创建工具
     * @param maker 语法创建工具
     */
    static void init(SyntaxTreeMaker maker) {
        if (null == maker) {
            return;
        }
        RegulationExecutor.maker = maker;
    }

    private RegulationExecutor(java.util.List<Regulation> regulations) {
        this.regulations = regulations;
    }

    /**
     * 接收规则创建处理器
     * @param regulations 规则处理链
     * @return 规则处理器
     */
    public static RegulationExecutor newExecutor(java.util.List<Regulation> regulations) {
        return new RegulationExecutor(regulations);
    }

    /**
     * 设置语句链存储处理结果，不设置则创建空语句链
     * @param statements 语句链
     * @return 规则处理器
     */
    public RegulationExecutor setStatement(ListBuffer<JCTree.JCStatement> statements) {
        this.statements = statements;
        return this;
    }

    /**
     * 设置失败分支动作，未设置则通过规则内部创建
     * @param action 动作语句
     * @return 规则处理器
     */
    public RegulationExecutor setAction(JCTree.JCStatement action) {
        this.action = action;
        return this;
    }

    /**
     * 接收语法树节点执行规则处理
     * @param self 数据节点
     * @param fieldName 字段名
     * @return 语句链
     */
    public ListBuffer<JCTree.JCStatement> execute(JCTree.JCExpression self, String fieldName) {
        if (null == statements) {
            statements = TreeUtil.newStatement();
        }

        for (Regulation regulation : regulations) {
            statements = regulation.handle(maker, statements, fieldName, self, action);
        }
        return statements;
    }
}
