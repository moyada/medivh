package io.moyada.medivh.processor;


import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import io.moyada.medivh.annotation.*;
import io.moyada.medivh.support.ElementOptions;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.util.ClassUtil;
import io.moyada.medivh.util.ElementUtil;
import io.moyada.medivh.visitor.CustomRuleTranslator;
import io.moyada.medivh.visitor.UtilMethodTranslator;
import io.moyada.medivh.visitor.ValidationTranslator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 校验注解处理器
 * @author xueyikang
 * @since 0.0.1
 **/
@SupportedAnnotationTypes("io.moyada.medivh.annotation.*")
public class ValidationGenerateProcessor extends AbstractProcessor {

    // 规则注解
    private List<Class<? extends Annotation>> ruleAnnos;

    // 信息输出体
    private Messager messager;

    // 处理器上下文
    private Context context;

    // 语法树
    private Trees trees;

    // 文件处理器
    private Filer filer;

    public ValidationGenerateProcessor() {
        ruleAnnos = new ArrayList<Class<? extends Annotation>>();
        ruleAnnos.add(Nullable.class);
        ruleAnnos.add(NotBlank.class);
        ruleAnnos.add(NotNull.class);
        ruleAnnos.add(DecimalMin.class);
        ruleAnnos.add(DecimalMax.class);
        ruleAnnos.add(Min.class);
        ruleAnnos.add(Max.class);
        ruleAnnos.add(Size.class);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        ClassUtil.disableJava9SillyWarning();

        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.filer = processingEnv.getFiler();
        this.trees = Trees.instance(processingEnv);
        this.messager = processingEnv.getMessager();

        messager.printMessage(Diagnostic.Kind.NOTE, "start generated validation processor");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> rootElements = roundEnv.getRootElements();
        // 获取校验方法
        Collection<? extends Element> methods = ElementUtil.getMethods(trees, rootElements, Throw.class.getName(), Return.class.getName());
        if (methods.isEmpty()) {
            return true;
        }

        // 获取对象规则
        Map<? extends Element, List<String>> classRules = ElementUtil.aggregateRule(roundEnv, ruleAnnos);

        SyntaxTreeMaker syntaxTreeMaker = SyntaxTreeMaker.newInstance(context);

        createUtilMethod(roundEnv, rootElements, syntaxTreeMaker);

        TreeTranslator translator;
        // 校验方法生成器
        if (!classRules.isEmpty()) {
            translator = new CustomRuleTranslator(syntaxTreeMaker, messager, classRules);
            for (Element element : classRules.keySet()) {
                JCTree tree = (JCTree) trees.getTree(element);
                tree.accept(translator);
            }
        }

        // 校验逻辑生成器
        translator = new ValidationTranslator(syntaxTreeMaker, messager);
        for (Element element : methods) {
            JCTree tree = (JCTree) trees.getTree(element);
            tree.accept(translator);
        }

        return true;
    }

    /**
     * 创建工具方法，如指定方法则不生效
     * 根据配置选择创建新文件提供方法，或者选择一个 public class 创建工具方法
     * @param roundEnv 根环境
     * @param elements 元素集合
     * @param syntaxTreeMaker 语句构造器
     */
    private void createUtilMethod(RoundEnvironment roundEnv, Collection<? extends Element> elements, SyntaxTreeMaker syntaxTreeMaker) {
        boolean createFile = !Boolean.FALSE.toString().equalsIgnoreCase(ElementOptions.UTIL_CREATE);
        if (createFile) {
            ElementUtil.createUtil(filer, roundEnv);
            messager.printMessage(Diagnostic.Kind.NOTE, "Created util class " + ElementOptions.UTIL_CLASS);
            return;
        }

        Element classElement = ElementUtil.findFirstPublicClass(elements);
        if (classElement == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "cannot find any public class");
            return;
        }

        JCTree tree = (JCTree) trees.getTree(classElement);
        tree.accept(new UtilMethodTranslator(syntaxTreeMaker, messager, classElement.toString()));
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
