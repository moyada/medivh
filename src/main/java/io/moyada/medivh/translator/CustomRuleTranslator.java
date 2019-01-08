package io.moyada.medivh.translator;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.ExpressionMaker;
import io.moyada.medivh.support.RegulationBuilder;
import io.moyada.medivh.regulation.LocalVariableRegulation;
import io.moyada.medivh.regulation.NotNullWrapperRegulation;
import io.moyada.medivh.regulation.NullCheckRegulation;
import io.moyada.medivh.regulation.Regulation;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.CheckUtil;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义对象校验方法生成器
 * 对定义 @link{Rule} 的类增加 CustomRuleTranslator.METHOD_NAME 的校验方法
 * @author xueyikang
 * @since 1.0
 **/
public class CustomRuleTranslator extends BaseTranslator {

    public CustomRuleTranslator(ExpressionMaker expressionMaker, Messager messager) {
        super(expressionMaker, messager);
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
            messager.printMessage(Diagnostic.Kind.ERROR, "[Param Error] unable to use " +
                    CTreeUtil.getOriginalTypeName(jcClassDecl.sym) + " (interface type) as parameter before JDK8 version.");
            return;
        }

        Symbol.ClassSymbol classSymbol = jcClassDecl.sym;
        String methodName = CheckUtil.getTmpMethod(classSymbol);

        // 解析所有参数规则
        Map<JCTree.JCExpression, java.util.List<Regulation>> rules = new HashMap<JCTree.JCExpression, java.util.List<Regulation>>();

        for (JCTree var : jcClassDecl.defs) {
            joinVarRules(rules, var);
        }
        if (rules.isEmpty()) {
            return;
        }

        String className = classSymbol.className();
        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Create " + methodName + " method in " + className);

        JCTree.JCBlock body = createBody(rules);
        JCTree.JCMethodDecl method = createMethod(methodName, body, isInterface);

        CheckUtil.addCheckMethod(className, methodName);

        // 刷新类信息
        jcClassDecl.defs = jcClassDecl.defs.append(method);
        this.result = jcClassDecl;
    }

    /**
     * 加入字段规则
     * @param rules 规则集合
     * @param var 当前元素节点
     */
    private void joinVarRules(Map<JCTree.JCExpression, java.util.List<Regulation>> rules, JCTree var) {
        Symbol symbol;
        String className;
        JCTree.JCExpression self;
        JCTree.JCVariableDecl localVar = null;

        // 变量
        if (var.getKind() == Tree.Kind.VARIABLE) {
            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) var;
            // 排除枚举
            if ((jcVariableDecl.mods.flags & Flags.ENUM) != 0) {
                return;
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
                return;
            }

            className = CTreeUtil.getReturnTypeName(methodDecl);
            // 过滤无返回值方法
            if (null == className) {
                return;
            }

            symbol = methodDecl.sym;

            JCTree.JCIdent ident = treeMaker.Ident(methodDecl.name);
            self = treeMaker.Apply(CTreeUtil.emptyParam(), ident, CTreeUtil.emptyParam());

            // 使用临时变量保存方法回调数据
            localVar = treeMaker.VarDef(treeMaker.Modifiers(0L), methodDecl.name, methodDecl.restype, self);
            self = treeMaker.Ident(localVar.name);
        } else {
            return;
        }

        byte classType = getClassType(className);
        java.util.List<Regulation> regulations = RegulationBuilder.findBasicRule(symbol, className, classType);

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
            return;
        }

        // 方法调用，增加临时变量规则
        if (localVar != null) {
            regulations.add(new LocalVariableRegulation(localVar));
        }

        rules.put(self, regulations);
    }

    /**
     * 创建代码块
     * @param ruleMap 规则集合
     * @return 代码块
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
                thisStatements = rule.handle(expressionMaker, thisStatements, name, self,null);
            }
            statements.append(getBlock(thisStatements));
        }

        // 校验通过返回 null
        JCTree.JCReturn returnStatement = treeMaker.Return(expressionMaker.nullNode);
        statements.append(returnStatement);
        return getBlock(statements);
    }

    /**
     * 创建校验方法
     * @param methodName 方法名
     * @param body 方法体
     * @param isInterface 是否接口类型
     * @return 方法元素
     */
    private JCTree.JCMethodDecl createMethod(String methodName, JCTree.JCBlock body, boolean isInterface) {
        List<JCTree.JCTypeParameter> param = List.nil();
        List<JCTree.JCVariableDecl> var = List.nil();
        List<JCTree.JCExpression> thrown = List.nil();
        return treeMaker.MethodDef(treeMaker.Modifiers(CTreeUtil.getNewMethodFlag(isInterface)),
                CTreeUtil.getName(namesInstance, methodName),
                expressionMaker.findClass(String.class.getName()),
                param, var, thrown,
                body, null);
    }
}
