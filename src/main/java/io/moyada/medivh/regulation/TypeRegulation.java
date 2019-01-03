package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.TypeUtil;

/**
 * 类型取值规则
 * @author xueyikang
 * @since 1.0
 **/
public abstract class TypeRegulation extends BaseRegulation {

    // 类型
    private byte type;

    public TypeRegulation(byte type) {
        this.type = type;
    }

    /**
     * 取值方式
     * @return
     */
    final String getMode() {
        return TypeUtil.getMode(type);
    }

    /**
     * 根据类型取值
     * @param makerContext
     * @param origin
     * @return
     */
    final JCTree.JCExpression getExpr(MakerContext makerContext, JCTree.JCExpression origin) {

        TreeMaker treeMaker = makerContext.getTreeMaker();
        // 获取大小信息
        JCTree.JCExpression out;

        switch (type) {
            case TypeUtil.STRING:
                out = treeMaker.Exec(makerContext.getMethod(origin, "length", CTreeUtil.emptyParam())).getExpression();
                break;
            case TypeUtil.ARRAY:
                out = makerContext.getField(origin, "length");
                break;
            case TypeUtil.COLLECTION:
                out = treeMaker.Exec(makerContext.getMethod(origin, "size", CTreeUtil.emptyParam())).getExpression();
                break;
            default:
                out = origin;
        }

        return out;
    }
}
