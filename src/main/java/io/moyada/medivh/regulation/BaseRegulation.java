package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ActionData;
import io.moyada.medivh.support.ExpressionMaker;
import io.moyada.medivh.support.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 基础处理规则
 * @author xueyikang
 * @since 1.0
 **/
public abstract class BaseRegulation implements Regulation {

    // 新动作模式
    private byte newActionMode = RETURN_STR;

    // 新动作的信息
    protected String info;

    // 新动作的数据
    private ActionData actionData;

    // 返回字符串
    public static final byte RETURN_STR = 0;
    // 抛出异常
    public static final byte THROW = 1;

    /**
     * 设置处理数据
     * @param actionData 处理数据
     */
    public void setActionData(ActionData actionData) {
        if (null == actionData) {
            return;
        }
        this.newActionMode = actionData.getActionMode();
        this.actionData = actionData;
    }

    @Override
    public ListBuffer<JCTree.JCStatement> handle(ExpressionMaker expressionMaker, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCStatement action) {
        action = createActionIfNull(action, expressionMaker, fieldName);
        JCTree.JCStatement exec = doHandle(expressionMaker, statements, self, action);
        statements.append(exec);
        return statements;
    }

    /**
     * 处理规则事件，返回构建语句
     * @param expressionMaker 语句构造器
     * @param statements 语句链
     * @param self 处理元素
     * @param action 执行事件
     * @return 处理语句
     */
    abstract JCTree.JCStatement doHandle(ExpressionMaker expressionMaker, ListBuffer<JCTree.JCStatement> statements,
                                         JCTree.JCExpression self, JCTree.JCStatement action);

    /**
     * 构建输出信息
     * @param fieldName 元素名称
     * @return 构建信息
     */
    abstract String buildInfo(String fieldName);

    /**
     * 无提供执行则创建处理方式
     * @param action 处理方式
     * @param expressionMaker 语句构造器
     * @param info 信息
     * @return 处理语句
     */
    JCTree.JCStatement createActionIfNull(JCTree.JCStatement action, ExpressionMaker expressionMaker, String info) {
        if (null != action) {
            return action;
        }
        this.info = buildInfo(info);
        return createAction(expressionMaker, this.info);
    }

    /**
     * 创建执行语句
     * @param expressionMaker 语句构造器
     * @param info 信息
     * @return 处理语句
     */
    JCTree.JCStatement createAction(ExpressionMaker expressionMaker, String info) {
        JCTree.JCStatement action;
        switch (newActionMode) {
            case THROW:
                JCTree.JCLiteral message = CTreeUtil.newElement(expressionMaker.getTreeMaker(),
                        TypeTag.CLASS,actionData.getInfo() + info);
                action = expressionMaker.newMsgThrow(actionData.getClassName(), message);
                break;
            default:
                action = expressionMaker.Return(TypeTag.CLASS, info);
        }
        return action;
    }
}
