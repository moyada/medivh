package io.moyada.medivh.visitor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.support.SyntaxTreeMaker;
import io.moyada.medivh.support.TypeTag;
import io.moyada.medivh.util.TreeUtil;
import io.moyada.medivh.util.CheckUtil;
import io.moyada.medivh.util.TypeUtil;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;

/**
 * 基础监视器
 * @author xueyikang
 * @since 1.0
 **/
abstract class BaseTranslator extends TreeTranslator {

    // 信息输出提供器
    final Messager messager;

    // 语法创建工具
    protected final SyntaxTreeMaker syntaxTreeMaker;

    // 语法树构造器
    final TreeMaker treeMaker;
    // 元素获取器
    final JavacElements javacElements;
    // 名称构造器
    final Object namesInstance ;

    BaseTranslator(SyntaxTreeMaker syntaxTreeMaker, Messager messager) {
        this.syntaxTreeMaker = syntaxTreeMaker;
        this.messager = messager;

        this.treeMaker = syntaxTreeMaker.getTreeMaker();
        this.namesInstance = syntaxTreeMaker.getNamesInstance();
        this.javacElements = syntaxTreeMaker.getJavacElements();
    }

    /**
     * 检测异常类是否包含 String 构造函数
     * @param exceptionTypeName 异常类名
     * @return 无字符串构造函数则返回 false
     */
    boolean checkException(String exceptionTypeName) {
        Symbol.ClassSymbol typeElement = javacElements.getTypeElement(exceptionTypeName);
        for (Symbol element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            List<Symbol.VarSymbol> parameters = ((Symbol.MethodSymbol) element).getParameters();
            if (parameters.size() != 1) {
                continue;
            }
            if (parameters.get(0).asType().toString().equals("java.lang.String")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取类型
     * @param className 类型名称
     * @return 类型
     */
    byte getClassType(String className) {
        // 字符串逻辑
        if (isString(className)) {
            return TypeUtil.STRING;
        }
        // 数组规则
        if (TypeUtil.isArr(className)) {
            return TypeUtil.ARRAY;
        }
        // 集合 Collection \ Map
        if (isCollection(className)) {
            return TypeUtil.COLLECTION;
        }
        if (TypeUtil.isPrimitive(className)) {
            return TypeUtil.PRIMITIVE;
        }

        return TypeUtil.OBJECT;
    }

    /**
     * 类型是否属于 {@link CharSequence} 或实现类
     * @param className 类型名称
     * @return 是否属于字符序列
     */
    private boolean isString(String className) {
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(className);
        // primitive 类型
        if (null == classSymbol) {
            return false;
        }

        if (classSymbol.isInterface()) {
            return isString(classSymbol);
        } else {
            List<Type> interfaces = classSymbol.getInterfaces();
            int size = interfaces.size();
            if (size == 0) {
                return false;
            }

            Symbol.TypeSymbol typeSymbol;
            // 检查接口
            for (int i = 0; i < size; i++) {
                typeSymbol = interfaces.get(i).tsym;
                if (isString(typeSymbol)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 类型是否属于集合或子类
     * @param className 类型名称
     * @return 是否属于集合
     */
    private boolean isCollection(String className) {
        Symbol.ClassSymbol classSymbol = syntaxTreeMaker.getTypeElement(className);
        // primitive 类型
        if (null == classSymbol) {
            return false;
        }

        if (classSymbol.isInterface()) {
            return isCollection(classSymbol);
        } else {
            List<Type> interfaces = classSymbol.getInterfaces();
            int size = interfaces.size();
            if (size == 0) {
                return false;
            }

            Symbol.TypeSymbol typeSymbol;
            // 检查接口
            for (int i = 0; i < size; i++) {
                typeSymbol = interfaces.get(i).tsym;
                if (isCollection(typeSymbol)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否是目标类或子类或实现类
     * @param className 类型名称
     * @param targetClass 目标类型
     * @return 类型是否相似
     */
    boolean isSubClass(String className, String targetClass) {
        if (className.equals(targetClass)) {
            return true;
        }

        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(className);
        // primitive 类型
        if (null == classSymbol) {
            return false;
        }

        Symbol.ClassSymbol typeElement = javacElements.getTypeElement(targetClass);
        // primitive 类型
        if (null == typeElement) {
            return false;
        }
        if (isInstanceOf(classSymbol, typeElement)) {
            return true;
        }
        return false;
    }

    /**
     * 判断类型是否为集合
     * @param typeSymbol 类型元素
     * @return 是否属于 Collection 或 Map
     */
    private boolean isCollection(Symbol typeSymbol) {
        return isInstanceOf(typeSymbol, syntaxTreeMaker.getCollectionSymbol()) || isInstanceOf(typeSymbol, syntaxTreeMaker.getMapSymbol());
    }

    /**
     * 判断类型是否为字符序列
     * @param typeSymbol 类型元素
     * @return 是否属于 CharSequence
     */
    private boolean isString(Symbol typeSymbol) {
        return isInstanceOf(typeSymbol, syntaxTreeMaker.getStringSymbol());
    }

    /**
     * 判断是否为类型的子类
     * @param typeSymbol 类型元素
     * @param classSymbol 类元素
     * @return 是否相似类型
     */
    private boolean isInstanceOf(Symbol typeSymbol, Symbol.ClassSymbol classSymbol) {
        return typeSymbol.isSubClass(classSymbol, syntaxTreeMaker.getTypes());
    }

    /**
     * 获取表达式的代码块
     * @param statements 表达式语句链
     * @return 代码块
     */
    JCTree.JCBlock getBlock(ListBuffer<JCTree.JCStatement> statements) {
        return treeMaker.Block(0, statements.toList());
    }

    /**
     * 替换方法内容
     * @param methodDecl 方法节点
     * @param body 方法体
     * @return 替换方法
     */
    JCTree.JCMethodDecl replaceMethod(JCTree.JCMethodDecl methodDecl, JCTree.JCBlock body) {
        return treeMaker.MethodDef(methodDecl.mods,
                methodDecl.name,
                methodDecl.restype,
                methodDecl.typarams,
                methodDecl.params,
                methodDecl.thrown,
                body, methodDecl.defaultValue);
    }

    /**
     * 参数转换
     * @param classSymbol 解析类节点
     * @param isConstruct 解析构造方法还是静态方法
     * @param values 参数数据
     * @return 返回对应参数元素
     */
    List<JCTree.JCExpression> getParamType(Symbol.ClassSymbol classSymbol, boolean isConstruct, String[] values) {
        int length = values.length;

        List<JCTree.JCExpression> param = null;

        boolean findParam;
        for (Symbol element : classSymbol.getEnclosedElements()) {

            // 构造方法
            if (isConstruct) {
                if (!element.isConstructor()) {
                    continue;
                }
            } else {
                // 静态方法
                if (element.getKind() != ElementKind.METHOD) {
                    continue;
                }
                if (!element.isStatic()) {
                    continue;
                }
            }

            Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
            List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();

            // 参数个数一致
            if (parameters.size() != values.length) {
                continue;
            }

            findParam = true;
            for (int i = 0; findParam && i < length; i++) {
                Symbol.VarSymbol varSymbol = parameters.get(i);
                String value = values[i];

                String typeName = TreeUtil.getOriginalTypeName(varSymbol);
                TypeTag baseType = TypeUtil.getBaseType(typeName);

                JCTree.JCExpression argsVal;

                if (CheckUtil.isNull(value)) {
                    // 原生类型无法返回 null
                    if (TypeUtil.isPrimitive(typeName)) {
                        param = null;
                        findParam = false;
                        continue;
                    }
                    argsVal = syntaxTreeMaker.nullNode;
                } else {
                    // 不支持复杂对象
                    if (null == baseType) {
                        param = null;
                        findParam = false;
                        continue;
                    }

                    Object data = TreeUtil.getValue(baseType, value);
                    // 数据与类型不匹配
                    if (null == data) {
                        param = null;
                        findParam = false;
                        continue;
                    }
                    argsVal = syntaxTreeMaker.newElement(baseType, data);
                }

                if (null == param) {
                    param = List.of(argsVal);
                } else {
                    param = param.append(argsVal);
                }
            }

            if (param != null) {
                return param;
            }
        }

        return null;
    }
}
