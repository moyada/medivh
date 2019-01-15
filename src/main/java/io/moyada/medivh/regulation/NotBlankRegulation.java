package io.moyada.medivh.regulation;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.TypeTag;

/**
 * 非空白字符串校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NotBlankRegulation extends BaseRegulation implements Regulation {

    @Override
    JCTree.JCStatement doHandle(SyntaxTreeMaker syntaxTreeMaker, ListBuffer<JCTree.JCStatement> statements,
                                JCTree.JCExpression self, JCTree.JCStatement action) {
        TreeMaker treeMaker = syntaxTreeMaker.getTreeMaker();

        // 调用方法进行校验
        JCTree.JCExpression aClass = syntaxTreeMaker.findClass(ElementOptions.UTIL_CLASS);
        JCTree.JCMethodInvocation isBlank = syntaxTreeMaker.getMethod(aClass, ElementOptions.BLANK_METHOD, List.of(self));

        // 返回值为 true 执行动作语句
        JCTree.JCExpression condition = syntaxTreeMaker.newBinary(TypeTag.EQ, isBlank, syntaxTreeMaker.trueNode);
        return treeMaker.If(condition, action, null);
    }

    @Override
    String buildInfo(String fieldName) {
        return fieldName + " " + ElementOptions.BLANK_INFO;
    }
}
