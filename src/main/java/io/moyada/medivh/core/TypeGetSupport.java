package io.moyada.medivh.core;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.TypeUtil;

/**
 * 类型取值规则
 * @author xueyikang
 * @since 1.0
 **/
public class TypeGetSupport {

    // 类型
    private final byte type;

    public TypeGetSupport(byte type) {
        this.type = type;
    }

    /**
     * 取值方式
     * @return
     */
    public final String getMode() {
        return TypeUtil.getMode(type);
    }

    /**
     * 根据类型取值
     * @param makerContext
     * @param origin
     * @return
     */
    public final JCTree.JCExpression getExpr(MakerContext makerContext, JCTree.JCExpression origin) {
        TreeMaker treeMaker = makerContext.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression out;

        switch (type) {
            case TypeUtil.STRING:
                out = treeMaker.Exec(makerContext.getMethod(origin, "length", CTreeUtil.emptyParam())).getExpression();
                break;
            case TypeUtil.ARRAY:
                out = makerContext.Select(origin, "length");
                break;
            case TypeUtil.COLLECTION:
                out = treeMaker.Exec(makerContext.getMethod(origin, "size", CTreeUtil.emptyParam())).getExpression();
                break;
            default:
                return origin;
        }
        return out;
    }
}
