package io.moyada.medivh.core;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import io.moyada.medivh.util.CTreeUtil;

import java.util.Collection;
import java.util.Map;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MakerContext {

    private final TreeMaker treeMaker;
    private final JavacElements javacElements;
    private final Object namesInstance;
    private final Types types;

    // null 表达式
    public final JCTree.JCLiteral nullNode;

    // true 表达式
    public final JCTree.JCLiteral trueNode;

    // false 表达式
    public final JCTree.JCLiteral falseNode;

    // collection
    final Symbol.ClassSymbol collectionSymbol;
    // map
    final Symbol.ClassSymbol mapSymbol;
    
    private MakerContext(Context context) {
        this.treeMaker = TreeMaker.instance(context);
        this.namesInstance = CTreeUtil.newInstanceForName(context);

        this.javacElements = JavacElements.instance(context);
        this.types = Types.instance(context);

        this.nullNode = CTreeUtil.newElement(treeMaker, TypeTag.BOT, null);
        this.trueNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 1);
        this.falseNode = CTreeUtil.newElement(treeMaker, TypeTag.BOOLEAN, 0);

        collectionSymbol = javacElements.getTypeElement(Collection.class.getName());
        mapSymbol = javacElements.getTypeElement(Map.class.getName());
    }

    public JCTree.JCReturn returnStr(String info) {
        return treeMaker.Return(CTreeUtil.newElement(treeMaker, TypeTag.CLASS, info));
    }

    /**
     * 获取属性
     * @param field
     * @param name
     * @return
     */
    public JCTree.JCFieldAccess getField(JCTree.JCExpression field, String name) {
        return treeMaker.Select(field, CTreeUtil.getName(namesInstance, name));
    }

    /**
     * 获取方法
     * @param field
     * @param method
     * @param param
     * @return
     */
    public JCTree.JCMethodInvocation getMethod(JCTree.JCExpression field, String method, List<JCTree.JCExpression> param) {
        return treeMaker.Apply(CTreeUtil.emptyParam(),
                getField(field, method),
                param
        );
    }

    /**
     * 查询类引用
     * @param className
     * @return
     */
    public JCTree.JCExpression findClass(String className) {
        String[] elems = className.split("\\.");

        Name name = CTreeUtil.getName(namesInstance, elems[0]);
        JCTree.JCExpression e = treeMaker.Ident(name);
        for (int i = 1 ; i < elems.length ; i++) {
            name = CTreeUtil.getName(namesInstance, elems[i]);
            e = e == null ? treeMaker.Ident(name) : treeMaker.Select(e, name);
        }

        return e;
    }

    public static MakerContext newInstance(Context context) {
        return new MakerContext(context);
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public JavacElements getJavacElements() {
        return javacElements;
    }

    public Object getNamesInstance() {
        return namesInstance;
    }

    public Types getTypes() {
        return types;
    }

    public Symbol.ClassSymbol getCollectionSymbol() {
        return collectionSymbol;
    }

    public Symbol.ClassSymbol getMapSymbol() {
        return mapSymbol;
    }
}
