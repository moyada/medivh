package io.moyada.medivh.support;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.TypeUtil;

/**
 * 类型取值规则
 * @author xueyikang
 * @since 1.0
 **/
public class TypeFetchSupport {

    // 类型
    private final byte type;

    public TypeFetchSupport(byte type) {
        this.type = type;
    }

    /**
     * 根据类型返回取值方式信息
     * @return 方法名
     */
    public final String getMode() {
        String mode;
        switch (type) {
            case TypeUtil.STRING:
                mode = ".length()";
                break;
            case TypeUtil.ARRAY:
                mode = ".length";
                break;
            case TypeUtil.COLLECTION:
                mode = ".size()";
                break;
            default:
                mode = "";
        }
        return mode;
    }

    /**
     * 根据类型取值方式
     * @param expressionMaker 语句创建器
     * @param origin 源对象
     * @return 语句元素
     */
    public final JCTree.JCExpression getExpr(ExpressionMaker expressionMaker, JCTree.JCExpression origin) {
        TreeMaker treeMaker = expressionMaker.getTreeMaker();

        // 获取大小信息
        JCTree.JCExpression out;

        switch (type) {
            case TypeUtil.STRING:
                out = treeMaker.Exec(expressionMaker.getMethod(origin, "length", CTreeUtil.emptyParam())).getExpression();
                break;
            case TypeUtil.ARRAY:
                out = expressionMaker.Select(origin, "length");
                break;
            case TypeUtil.COLLECTION:
                out = treeMaker.Exec(expressionMaker.getMethod(origin, "size", CTreeUtil.emptyParam())).getExpression();
                break;
            default:
                return origin;
        }
        return out;
    }
}
