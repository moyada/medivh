package io.moyada.medivh.visitor;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * 坐标扫描器
 * @author xueyikang
 * @since 1.3.1
 **/
public class PosScanner extends TreeScanner {

    // 类节点
    private final JCTree.JCClassDecl classNode;

    PosScanner(JCTree.JCClassDecl classNode) {
        if (null == classNode) {
            throw new NullPointerException("Parameter \"JCClassDecl\" can't be null");
        }
        this.classNode = classNode;
    }

    /**
     * 为当前类内语法树节点分配坐标
     * @param tree 子节点
     */
    private void visit(JCTree tree) {
        if (null == tree) {
            return;
        }
        tree.pos = getNextPos();
    }

    /**
     * 获取下一个可用坐标
     * @return 坐标索引
     */
    private int getNextPos() {
        int pos = classNode.getPreferredPosition();
        classNode.setPos(pos+1);
        return pos;
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        visit(jcMethodDecl);
        super.visitMethodDef(jcMethodDecl);
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        visit(jcVariableDecl);
        super.visitVarDef(jcVariableDecl);
    }

    @Override
    public void visitParens(JCTree.JCParens jcParens) {
        visit(jcParens);
        super.visitParens(jcParens);
    }

    @Override
    public void visitTypeParameter(JCTree.JCTypeParameter jcTypeParameter) {
        visit(jcTypeParameter);
        super.visitTypeParameter(jcTypeParameter);
    }

    @Override
    public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
        visit(jcAnnotation);
        super.visitAnnotation(jcAnnotation);
    }
}
