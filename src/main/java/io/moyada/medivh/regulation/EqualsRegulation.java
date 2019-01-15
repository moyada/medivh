package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.TypeFetchSupport;
import io.moyada.medivh.support.TypeTag;

/**
 * 比较规则
 * @author xueyikang
 * @since 1.0
 **/
public class EqualsRegulation extends BaseRegulation implements Regulation {

    // 类型
    private TypeTag typeTag;
    // 值对象
    private Object value;

    // 类型数据获取
    private TypeFetchSupport typeFetchSupport;

    // 值语句
    private JCTree.JCExpression valueExpr;

    // 比较方式
    private TypeTag compareTag;

    public EqualsRegulation(byte type, boolean equals) {
        this(type, TypeTag.BOT, null ,equals);
    }

    public EqualsRegulation(byte type, TypeTag typeTag, Object value, boolean equals) {
        this.typeTag = typeTag;
        this.value = value;
        if (equals) {
            this.compareTag = TypeTag.EQ;
        } else {
            this.compareTag = TypeTag.NE;
        }
        this.typeFetchSupport = new TypeFetchSupport(type);
    }

    @Override
    JCTree.JCStatement doHandle(SyntaxTreeMaker syntaxTreeMaker, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = syntaxTreeMaker.getTreeMaker();

        // 使用固定值比较
        JCTree.JCExpression rival = getValue(syntaxTreeMaker);

        JCTree.JCExpression condition = syntaxTreeMaker.newBinary(compareTag,
                typeFetchSupport.getExpr(syntaxTreeMaker, self), rival);
        return treeMaker.If(condition, action, null);
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + ElementOptions.EQUALS_INFO + " " + value;
    }

    /**
     * 获取数据语句
     * @param syntaxTreeMaker 语法创建工具
     * @return 数据语句元素
     */
    private JCTree.JCExpression getValue(SyntaxTreeMaker syntaxTreeMaker) {
        if (null == valueExpr) {
            valueExpr = syntaxTreeMaker.newElement(typeTag, value);
        }
        return valueExpr;
    }
}
