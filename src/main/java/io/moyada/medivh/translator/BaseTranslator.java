package io.moyada.medivh.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.TypeTag;
import io.moyada.medivh.util.CTreeUtil;
import io.moyada.medivh.util.CheckUtil;
import io.moyada.medivh.util.TypeUtil;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ElementKind;

/**
 * @author xueyikang
 * @since 1.0
 **/
class BaseTranslator extends TreeTranslator {

    protected final Messager messager;

    protected final MakerContext makerContext;

    final TreeMaker treeMaker;
    final JavacElements javacElements;
    final Object namesInstance ;

    BaseTranslator(MakerContext makerContext, Messager messager) {
        this.makerContext = makerContext;
        this.messager = messager;

        this.treeMaker = makerContext.getTreeMaker();
        this.namesInstance = makerContext.getNamesInstance();
        this.javacElements = makerContext.getJavacElements();
    }

    /**
     * 检测异常类是否包含 String 构造函数
     * @param exceptionTypeName
     * @return
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
     * @param className
     * @return
     */
    byte getClassType(String className) {
        // 字符串逻辑
        if (TypeUtil.isStr(className)) {
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
     * 是否属于集合或子类
     * @param className
     * @return
     */
    boolean isCollection(String className) {
        Symbol.ClassSymbol classSymbol = javacElements.getTypeElement(className);
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
     * @param className
     * @param targetClass
     * @return
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
     * @param typeSymbol
     * @return
     */
    private boolean isCollection(Symbol typeSymbol) {
        return isInstanceOf(typeSymbol, makerContext.getCollectionSymbol()) || isInstanceOf(typeSymbol, makerContext.getMapSymbol());
    }

    /**
     * 判断是否为类型的子类
     * @param typeSymbol
     * @param classSymbol
     * @return
     */
    boolean isInstanceOf(Symbol typeSymbol, Symbol.ClassSymbol classSymbol) {
        return typeSymbol.isSubClass(classSymbol, makerContext.getTypes());
    }

    /**
     * 获取表达式的代码块
     * @param statements
     * @return
     */
    JCTree.JCBlock getBlock(ListBuffer<JCTree.JCStatement> statements) {
        return treeMaker.Block(0, statements.toList());
    }

    /**
     * 替换方法内容
     * @param methodDecl
     * @param body
     * @return
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
     * 创建新变量
     * @param name
     * @param flags
     * @param type
     * @param init
     * @return
     */
    JCTree.JCVariableDecl newVar(String name, long flags, String type, JCTree.JCExpression init) {
        return treeMaker.VarDef(treeMaker.Modifiers(flags),
                CTreeUtil.getName(namesInstance, name),
                makerContext.findClass(type), init);
    }

    /**
     * 参数转换
     * @param classSymbol
     * @param values
     * @return
     */
    List<JCTree.JCExpression> getParamType(Symbol.ClassSymbol classSymbol, String[] values) {
        int length = values.length;

        List<JCTree.JCExpression> param = null;

        boolean findParam;
        for (Symbol element : classSymbol.getEnclosedElements()) {
            // 构造方法
            if (element.isConstructor()) { // && (element.flags() & Flags.PUBLIC) != 0) {
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

                    JCTree.JCExpression argsVal;

                    String typeName = CTreeUtil.getOriginalTypeName(varSymbol);
                    TypeTag baseType = TypeUtil.getBaseType(typeName);

                    if (CheckUtil.isNull(value)) {
                        if (TypeUtil.isPrimitive(typeName)) {
                            param = null;
                            findParam = false;
                            continue;
                        }
                        argsVal = makerContext.nullNode;
                    } else {
                        // 不支持复杂对象
                        if (null == baseType) {
                            param = null;
                            findParam = false;
                            continue;
                        }

                        Object data = CTreeUtil.getValue(baseType, value);
                        // 数据与类型不匹配
                        if (null == data) {
                            param = null;
                            findParam = false;
                            continue;
                        }
                        argsVal = CTreeUtil.newElement(treeMaker, baseType, data);
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
        }

        return null;
    }

}
