package io.moyada.medivh.visitor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import io.moyada.medivh.regulation.LocalVariableRegulation;
import io.moyada.medivh.regulation.NotNullWrapperRegulation;
import io.moyada.medivh.regulation.NullCheckRegulation;
import io.moyada.medivh.regulation.Regulation;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.RegulationBuilder;
import io.moyada.medivh.support.RegulationExecutor;
import io.moyada.medivh.util.TreeUtil;
import io.moyada.medivh.util.CheckUtil;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
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

    private Map<String, java.util.List<String>> ruleItems;

    public CustomRuleTranslator(SyntaxTreeMaker syntaxTreeMaker, Messager messager,
                                Map<? extends Element, java.util.List<String>> classRules) {
        super(syntaxTreeMaker, messager);

        ruleItems = new HashMap<String, java.util.List<String>>(classRules.size());
        for (Map.Entry<? extends Element, java.util.List<String>> classRule : classRules.entrySet()) {
            String className = classRule.getKey().toString();
            java.util.List<String> items = classRule.getValue();
            ruleItems.put(className, items);
        }
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);

        boolean isInterface = (jcClassDecl.mods.flags & Flags.INTERFACE) != 0;
        // 过滤jdk8以下接口无法创建校验方法
        if (isInterface && !TreeUtil.hasDefaultInterface()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Param Error] Can't use " +
                    TreeUtil.getOriginalTypeName(jcClassDecl.sym) + " (interface type) as parameter before Java 8.");
            return;
        }

        Symbol.ClassSymbol classSymbol = jcClassDecl.sym;
        if (null == classSymbol) {
            return;
        }
        String className = classSymbol.className();
        java.util.List<String> items = ruleItems.get(className);
        if (null == items) {
            return;
        }

        // 解析所有参数规则
        Map<JCTree.JCExpression, java.util.List<Regulation>> rules = new HashMap<JCTree.JCExpression, java.util.List<Regulation>>();
        for (JCTree var : jcClassDecl.defs) {
            joinVarRules(items, rules, var);
        }
        if (rules.isEmpty()) {
            return;
        }

        // 创建校验方法
        String methodName = CheckUtil.getTmpMethod(classSymbol);
        JCTree.JCBlock body = createBody(rules);
        JCTree.JCMethodDecl method = createMethod(methodName, body, isInterface);

        CheckUtil.addCheckMethod(className, methodName);

        // 刷新类信息
        jcClassDecl.defs = jcClassDecl.defs.append(method);
        this.result = jcClassDecl;

        messager.printMessage(Diagnostic.Kind.NOTE, "processing  =====>  Create method \"" + methodName + "\" in " + className);
    }

    /**
     * 加入字段规则
     * @param items 规则元素集合
     * @param rules 规则集合
     * @param var 当前元素节点
     */
    private void joinVarRules(java.util.List<String> items, Map<JCTree.JCExpression, java.util.List<Regulation>> rules, JCTree var) {
        Tree.Kind kind = var.getKind();

        Symbol symbol;
        String name;
        String typeName;
        JCTree.JCExpression self;
        JCTree.JCVariableDecl localVar = null;

        // 变量
        if (kind == Tree.Kind.VARIABLE) {
            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) var;
            // 排除枚举
            if ((jcVariableDecl.mods.flags & Flags.ENUM) != 0) {
                return;
            }

            symbol = jcVariableDecl.sym;
            typeName = TreeUtil.getOriginalTypeName(symbol);
            self = treeMaker.Ident(jcVariableDecl.name);
            name = jcVariableDecl.name.toString();
        }
        // 方法
        else if (kind == Tree.Kind.METHOD) {
            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) var;
            typeName = TreeUtil.getReturnTypeName(methodDecl);
            // 过滤无返回值方法或构造方法
            if (null == typeName) {
                return;
            }
            // 过滤带参方法
            if (!methodDecl.getParameters().isEmpty()) {
                return;
            }

            symbol = methodDecl.sym;

            // 获取方法引用
            Name methodName = methodDecl.name;
            JCTree.JCIdent ident = treeMaker.Ident(methodName);
            self = treeMaker.Apply(TreeUtil.emptyExpression(), ident, TreeUtil.emptyExpression());

            // 使用临时变量保存方法回调数据
            localVar = treeMaker.VarDef(treeMaker.Modifiers(0L), methodName, methodDecl.restype, self);
            self = treeMaker.Ident(localVar.name);

            name = methodName.toString() + "()";
        } else {
            return;
        }

        // 非规则元素
        if (!items.contains(name)) {
            return;
        }

        byte classType = getClassType(typeName);
        java.util.List<Regulation> regulations = RegulationBuilder.findBasicRule(symbol, typeName, classType);
        boolean notEmpty = !regulations.isEmpty();

        Boolean checkNull = RegulationBuilder.checkNotNull(symbol, classType, notEmpty);
        // 非原始类型增加空校验/非空包装
        if (null != checkNull) {
            if (checkNull) {
                regulations.add(new NullCheckRegulation());
            } else {
                // 无规则不使用非空包装
                if (notEmpty) {
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
        ListBuffer<JCTree.JCStatement> statements = TreeUtil.newStatement();


        for (Map.Entry<JCTree.JCExpression, java.util.List<Regulation>> entry : ruleMap.entrySet()) {
            JCTree.JCExpression self = entry.getKey();
            java.util.List<Regulation> regulations = entry.getValue();

            // 当前字段规则链
            ListBuffer<JCTree.JCStatement> thisStatements = RegulationExecutor
                    .newExecutor(regulations)
                    .execute(self, self.toString());

            statements.append(getBlock(thisStatements));
        }

        // 校验通过返回 null
        JCTree.JCReturn returnStatement = treeMaker.Return(syntaxTreeMaker.nullNode);
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
        return treeMaker.MethodDef(treeMaker.Modifiers(TreeUtil.getNewMethodFlag(isInterface)),
                syntaxTreeMaker.getName(methodName),
                syntaxTreeMaker.findClass(String.class.getName()),
                List.<JCTree.JCTypeParameter>nil(), List.<JCTree.JCVariableDecl>nil(), TreeUtil.emptyExpression(),
                body, null);
    }
}
