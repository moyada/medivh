package io.moyada.medivh.util;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import io.moyada.medivh.annotation.NotBlank;
import io.moyada.medivh.support.ElementOptions;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 元素工具
 * @author xueyikang
 * @since 1.3.1
 **/
public final class ElementUtil {

    private ElementUtil() {
    }

    /**
     * 获取待增强方法
     * @param trees 语法树
     * @param rootElements 根元素集合
     * @param annoNames 注解名称
     * @return 方法元素集合
     */
    public static Collection<? extends Element> getMethods(Trees trees, Set<? extends Element> rootElements, String... annoNames) {
        Set<Element> methods = new HashSet<Element>();

        boolean filter;
        for (Element rootElement : rootElements) {
            if (rootElement.getKind() == ElementKind.INTERFACE) {
                continue;
            }
            JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) trees.getTree(rootElement);
            boolean checkClass = CheckUtil.isCheckClass(classDecl);

            List<? extends Element> elements = rootElement.getEnclosedElements();
            for (Element element : elements) {
                if (element.getKind() != ElementKind.METHOD) {
                    continue;
                }
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) trees.getTree(element);
                // 无需校验的方法
                if (isJump(methodDecl)) {
                    continue;
                }
                // 无参数方法
                com.sun.tools.javac.util.List<JCTree.JCVariableDecl> parameters = methodDecl.getParameters();
                if (parameters.isEmpty()) {
                    continue;
                }

                // 标记排除
                if (CheckUtil.isExclusive(methodDecl.sym)) {
                    continue;
                }

                // 类上或方法上标记校验处理
                if (checkClass || CheckUtil.isCheckMethod(methodDecl)) {
                    methods.add(element);
                    continue;
                }

                filter = true;
                for (int i = 0; filter && i < parameters.size(); i++) {
                    JCTree.JCVariableDecl variableDecl = parameters.get(i);
                    List<? extends AnnotationMirror> mirrors = variableDecl.sym.getAnnotationMirrors();
                    if (mirrors.isEmpty()) {
                        continue;
                    }
                    for (int j = 0; filter && j < mirrors.size(); j++) {
                        String name = mirrors.get(j).getAnnotationType().toString();
                        for (int k = 0; filter && k < annoNames.length; k++) {
                            // 存在标记注解的字段，则将该方法记录，跳至下一方法
                            if (annoNames[k].equals(name)) {
                                methods.add(element);
                                filter = false;
                            }
                        }
                    }
                }
            }
        }

        return methods;
    }

    /**
     * 跳过 接口、抽象 方法
     * @param methodDecl 方法节点
     * @return 合法节点则返回 false
     */
    public static boolean isJump(JCTree.JCMethodDecl methodDecl) {
        if (null == methodDecl) {
            return true;
        }
        if ((methodDecl.sym.getEnclosingElement().flags() & Flags.INTERFACE) != 0) {
            return true;
        }
        if ((methodDecl.getModifiers().flags & Flags.ABSTRACT) != 0) {
            return true;
        }
        return false;
    }
    
    /**
     * 获取存在规则元素
     * @param roundEnv 环境
     * @param ruleAnnos 注解
     * @return 规则类元素与规则元素集合
     */
    public static Map<? extends Element, List<String>> aggregateRule(RoundEnvironment roundEnv, List<Class<? extends Annotation>> ruleAnnos) {
        Set<Element> rules = new HashSet<Element>();

        Set<? extends Element> elements;
        for (Class<? extends Annotation> anno : ruleAnnos) {
            elements = roundEnv.getElementsAnnotatedWith(anno);
            rules.addAll(ElementFilter.fieldsIn(elements));
            rules.addAll(ElementFilter.methodsIn(elements));
        }

        Map<Element, List<String>> classRule = new HashMap<Element, List<String>>();
        Element classEle;
        for (Element element : rules) {
            classEle = element.getEnclosingElement();
            List<String> items = classRule.get(classEle);
            if (null == items) {
                items = new ArrayList<String>();
                classRule.put(classEle, items);
            }
            items.add(element.toString());
        }

        return classRule;
    }

    /**
     * 创建工具类
     * @param filer  文件处理器
     * @param roundEnv 根环境
     */
    public static void createUtil(Filer filer, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(NotBlank.class);
        if (annotatedWith.isEmpty()) {
            return;
        }

        String packageName = getPackage(annotatedWith);
        if (null == packageName) {
            return;
        }

        String className = "Util";
        try {
            SystemUtil.createFile(filer, packageName, className,"META-INF/Util.rs");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ElementOptions.UTIL_CLASS = packageName + "." + className;
    }

    /**
     * 获取元素集合中其中一个的包名
     * @param elements 元素集合
     * @return 包名
     */
    private static String getPackage(Set<? extends Element> elements) {
        Element enclosingElement;
        for (Element element : elements) {
            enclosingElement = element.getEnclosingElement();
            while (enclosingElement != null && enclosingElement.getKind() != ElementKind.PACKAGE) {
                enclosingElement = enclosingElement.getEnclosingElement();
            }
            if (null != enclosingElement) {
                return enclosingElement.toString();
            }
        }
        return null;
    }

    /**
     * 选择一个 public class 创建工具方法
     * @param elements 元素集合
     * @return 类元素
     */
    public static Element findFirstPublicClass(Collection<? extends Element> elements) {
        List<TypeElement> typeElements = ElementFilter.typesIn(elements);
        if (typeElements.isEmpty()) {
            return null;
        }
        TypeElement classElement = null;
        for (TypeElement element : typeElements) {
            ElementKind kind = element.getKind();
            if (kind != ElementKind.CLASS) {
                continue;
            }
            if (!isPublic(element)) {
                continue;
            }
            // 排除内部类
            if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                continue;
            }

            classElement = element;
        }

        return classElement;
    }

    /**
     * 是否是public类型
     * @param element 元素
     * @return 存在 Public 标识则返回 true
     */
    private static boolean isPublic(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        for (Modifier modifier : modifiers) {
            if (modifier == Modifier.PUBLIC) {
                return true;
            }
        }
        return false;
    }
}
