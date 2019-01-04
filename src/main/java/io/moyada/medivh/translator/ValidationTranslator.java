package io.moyada.medivh.translator;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.annotation.Variable;
import io.moyada.medivh.core.Element;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.RegulationBuilder;
import io.moyada.medivh.regulation.NotNullWrapperRegulation;
import io.moyada.medivh.regulation.NullCheckRegulation;
import io.moyada.medivh.regulation.Regulation;
import io.moyada.medivh.util.CTreeUtil;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;

/**
 * 校验方法生成器
 * 对定义 @link{Rule} 的类增加 ValidationTranslator.METHOD_NAME 的校验方法
 * @author xueyikang
 * @since 1.0
 **/
public class ValidationTranslator extends BaseTranslator {

    public ValidationTranslator(MakerContext makerContext, Messager messager) {
        super(makerContext, messager);
    }

    /**
     * 扫描类节点
     * @param jcClassDecl
     */
    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        boolean isInterface = (jcClassDecl.mods.flags & Flags.INTERFACE) != 0;
        // 过滤jdk8以下接口无法创建校验方法
        if (isInterface && !CTreeUtil.isDefaultInterface()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Param Error] unable to use interface type param before JDK8 version.");
            return;
        }

        Variable annotation = CTreeUtil.getAnnotation(jcClassDecl.sym, Variable.class);
        String methodName = Element.getTmpMethod(annotation);

        // 解析所有参数规则
        Map<JCTree.JCExpression, java.util.List<Regulation>> rules = new HashMap<JCTree.JCExpression, java.util.List<Regulation>>();

        for (JCTree var : jcClassDecl.defs) {
            Symbol symbol;
            String className;
            JCTree.JCExpression self;

            // 变量
            if (var.getKind() == Tree.Kind.VARIABLE) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) var;
                // 排除枚举
                if ((jcVariableDecl.mods.flags & Flags.ENUM) != 0) {
                    continue;
                }

                symbol = jcVariableDecl.sym;
                className = CTreeUtil.getOriginalTypeName(symbol);
                self = treeMaker.Ident(jcVariableDecl.name);
            }
            // 方法
            else if (var.getKind() == Tree.Kind.METHOD) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) var;
                // 过滤带参方法
                if (!methodDecl.getParameters().isEmpty()) {
                    continue;
                }

                className = CTreeUtil.getReturnTypeName(methodDecl);
                // 过滤无返回值方法
                if (null == className) {
                    continue;
                }

                symbol = methodDecl.sym;

                JCTree.JCIdent ident = treeMaker.Ident(methodDecl.name);
                self = treeMaker.Apply(CTreeUtil.emptyParam(), ident, CTreeUtil.emptyParam());
            } else {
                continue;
            }

            byte classType = getClassType(className);
            java.util.List<Regulation> regulations =
                    RegulationBuilder.findBasicRule(symbol, className, classType);

            Boolean checkNull = RegulationBuilder.checkNotNull(symbol, classType);
            // 非原始类型
            if (null != checkNull) {
                if (checkNull) {
                    regulations.add(new NullCheckRegulation());
                } else {
                    // 无规则不使用非空包装
                    if (!regulations.isEmpty()) {
                        regulations.add(new NotNullWrapperRegulation());
                    }
                }
            }

            if (regulations.isEmpty()) {
                continue;
            }
            rules.put(self, regulations);
        }
        if (rules.isEmpty()) {
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Create " + methodName + " method in " + jcClassDecl.sym.className());

        JCTree.JCBlock body = createBody(rules);
        JCTree.JCMethodDecl method = createMethod(body, methodName, isInterface);
        Element.setCheckMethod(CTreeUtil.getOriginalTypeName(jcClassDecl.sym), methodName);
        // 刷新类信息
        jcClassDecl.defs = jcClassDecl.defs.append(method);
        this.result = jcClassDecl;
    }

    /**
     * 创建代码块
     * @return
     */
    private JCTree.JCBlock createBody(Map<JCTree.JCExpression, java.util.List<Regulation>> ruleMap) {
        ListBuffer<JCTree.JCStatement> statements = CTreeUtil.newStatement();


        for (Map.Entry<JCTree.JCExpression, java.util.List<Regulation>> entry : ruleMap.entrySet()) {
            // 当前字段规则链
            ListBuffer<JCTree.JCStatement> thisStatements = CTreeUtil.newStatement();

            JCTree.JCExpression self = entry.getKey();
            java.util.List<Regulation> rules = entry.getValue();

            String name = self.toString();
            for (Regulation rule : rules) {
                thisStatements = rule.handle(makerContext, thisStatements, name, self,null);
            }
            statements.append(getBlock(thisStatements));
        }

        // 校验通过返回 null
        JCTree.JCReturn returnStatement = treeMaker.Return(makerContext.nullNode);
        statements.append(returnStatement);
        return getBlock(statements);
    }

    /**
     * 创建校验方法
     * @param body
     * @return
     */
    private JCTree.JCMethodDecl createMethod(JCTree.JCBlock body, String methodName, boolean isInterface) {
        List<JCTree.JCTypeParameter> param = List.nil();
        List<JCTree.JCVariableDecl> var = List.nil();
        List<JCTree.JCExpression> thrown = List.nil();
        return treeMaker.MethodDef(treeMaker.Modifiers(CTreeUtil.getNewMethodFlag(isInterface)),
                CTreeUtil.getName(namesInstance, methodName),
                makerContext.findClass(String.class.getName()),
                param, var, thrown,
                body, null);
    }
}
