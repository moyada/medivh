package io.moyada.medivh.support;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * 坐标扫描器
 * @author xueyikang
 * @since 1.3.1
 **/
public class PosScanner extends TreeScanner {

    private final JCTree.JCClassDecl classNode;

    public PosScanner(JCTree.JCClassDecl classNode) {
        this.classNode = classNode;
    }

    private void visit(JCTree tree) {
        if (null == tree) {
            return;
        }
        int pos = classNode.getPreferredPosition();
        tree.pos = pos;
        classNode.setPos(pos+1);
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
