package io.moyada.medivh.core;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;

/**
 * 临时变量存储支持，对需要通过多次方法调用获取大小的语句，使用临时变量存储调用返回。
 * @author xueyikang
 * @since 1.2.1
 **/
public class LocalVarSupport {

    // 变量名
    private final String name;
    // 类型
    private final TypeTag typeTag;

    // 临时变量
    private JCTree.JCIdent localValue;

    public LocalVarSupport(TypeTag typeTag) {
        this.name = "var$" + typeTag.ordinal();
        this.typeTag = typeTag;
        this.localValue = null;
    }

    /**
     * 使用临时变量保存取值
     * @param makerContext
     * @param origin
     * @return
     */
    public final JCTree.JCExpression getValue(MakerContext makerContext, ListBuffer<JCTree.JCStatement> statements, JCTree.JCExpression origin) {
        if (null == localValue) {
            TreeMaker treeMaker = makerContext.getTreeMaker();

            JCTree.JCVariableDecl localVar = makerContext.newLocalVar(name, typeTag, origin);
            localValue = treeMaker.Ident(localVar.name);

            statements.append(localVar);
        }

        return localValue;
    }
}
