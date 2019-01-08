package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.ActionData;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.util.CTreeUtil;

/**
 * 校验规则
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

    public void setActionData(ActionData actionData) {
        if (null == actionData) {
            return;
        }
        this.newActionMode = actionData.getActionMode();
        this.actionData = actionData;
    }

    @Override
    public ListBuffer<JCTree.JCStatement> handle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                                 String fieldName, JCTree.JCExpression self, JCTree.JCStatement action) {
        action = createActionIfNull(action, makerContext, fieldName);
        JCTree.JCStatement exec = doHandle(makerContext, statements, self, action);
        statements.append(exec);
        return statements;
    }

    /**
     * 处理规则事件，返回构建语句
     * @param makerContext
     * @param statements
     * @param self
     * @param action
     * @return
     */
    abstract JCTree.JCStatement doHandle(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements,
                                  JCTree.JCExpression self, JCTree.JCStatement action);

    /**
     * 构建输出信息
     * @param fieldName
     * @return
     */
    abstract String buildInfo(String fieldName);

    /**
     * 无提供执行则创建
     * @param action
     * @param makerContext
     * @param info
     * @return
     */
    JCTree.JCStatement createActionIfNull(JCTree.JCStatement action, MakerContext makerContext, String info) {
        if (null != action) {
            return action;
        }
        this.info = buildInfo(info);
        return createAction(makerContext, this.info);
    }

    /**
     * 获取执行语句
     * @param makerContext
     * @param info
     * @return
     */
    JCTree.JCStatement createAction(MakerContext makerContext, String info) {
        JCTree.JCStatement action;
        switch (newActionMode) {
            case THROW:
                JCTree.JCLiteral message = CTreeUtil.newElement(makerContext.getTreeMaker(),
                        TypeTag.CLASS,actionData.getInfo() + info);
                action = makerContext.newMsgThrow(message, actionData.getClassName());
                break;
            default:
                action = makerContext.Return(TypeTag.CLASS, info);
        }
        return action;
    }
}
