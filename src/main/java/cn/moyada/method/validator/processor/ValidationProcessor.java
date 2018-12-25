package cn.moyada.method.validator.processor;


import cn.moyada.method.validator.annotation.Check;
import cn.moyada.method.validator.annotation.Rule;
import cn.moyada.method.validator.annotation.Verify;
import cn.moyada.method.validator.translator.ValidationTranslator;
import cn.moyada.method.validator.translator.VerificationTranslator;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xueyikang
 * @since 0.0.1
 **/
public class ValidationProcessor extends AbstractProcessor {

    private Messager messager;

    private Context context;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.trees = Trees.instance(processingEnv);
        this.messager = processingEnv.getMessager();

        messager.printMessage(Diagnostic.Kind.NOTE, "start validate processor");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取校验方法
        Set<? extends Element> methods = ElementFilter.methodsIn(
                roundEnv.getElementsAnnotatedWith(Verify.class));
        if (methods.isEmpty()) {
            return true;
        }

        // 获取对象规则
        Set<? extends Element> rules = ElementFilter.fieldsIn(
                roundEnv.getElementsAnnotatedWith(Rule.class));
        if (rules.isEmpty()) {
            return true;
        }

        // 解析聚合类
        Collection<? extends Element> ruleClass = getClass(rules);
        if (ruleClass == null || ruleClass.isEmpty()) {
            return true;
        }

        // 校验方法生成器
        TreeTranslator translator = new VerificationTranslator(context, messager);
        for (Element element : ruleClass) {
            JCTree tree = (JCTree) trees.getTree(element);
            tree.accept(translator);
        }

        // 校验逻辑生成器
        translator = new ValidationTranslator(context, ruleClass, messager);
        for (Element element : methods) {
            JCTree tree = (JCTree) trees.getTree(element);
            tree.accept(translator);
        }

        return true;
    }

    private Collection<? extends Element> getClass(Set<? extends Element> rules) {
        if (rules.isEmpty()) {
            return null;
        }

        Set<Symbol.ClassSymbol> classRule = new HashSet<Symbol.ClassSymbol>();
        Symbol.VarSymbol var;
        for (Element rule : rules) {
            var = (Symbol.VarSymbol) rule;
            classRule.add(var.enclClass());
        }
        return classRule;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<String>(4);
        annotationTypes.add(Verify.class.getName());
        annotationTypes.add(Rule.class.getName());
        annotationTypes.add(Check.class.getName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_6) > 0) {
            return SourceVersion.latest();
        } else {
            return SourceVersion.RELEASE_6;
        }
    }
}
