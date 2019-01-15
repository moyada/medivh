package io.moyada.medivh.support;

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
        this.name = generateName(typeTag);
        this.typeTag = typeTag;
        this.localValue = null;
    }

    /**
     * 创建临时变量名称
     * @param typeTag 原生类型
     * @return 名称
     */
    private String generateName(TypeTag typeTag) {
        return "var$" + typeTag.ordinal();
    }

    /**
     * 使用临时变量保存取值
     * @param syntaxTreeMaker 语句创建器
     * @param statements 语句链
     * @param origin 取值方法
     * @return 临时变量语句
     */
    public final JCTree.JCExpression getValue(SyntaxTreeMaker syntaxTreeMaker, ListBuffer<JCTree.JCStatement> statements, JCTree.JCExpression origin) {
        if (null == localValue) {
            TreeMaker treeMaker = syntaxTreeMaker.getTreeMaker();

            JCTree.JCVariableDecl localVar = syntaxTreeMaker.newLocalVar(name, typeTag, origin);
            localValue = treeMaker.Ident(localVar.name);

            statements.append(localVar);
        }

        return localValue;
    }
}
