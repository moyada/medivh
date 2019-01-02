package io.moyada.medivh.regulation;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import io.moyada.medivh.core.MakerContext;
import io.moyada.medivh.core.RegulationBuilder;
import io.moyada.medivh.util.CTreeUtil;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MethodRegulationContext {

    private MakerContext makerContext;

    private String paramName;

    public MethodRegulationContext(MakerContext makerContext) {
        this.makerContext = makerContext;
    }

    private Regulation nullRegulation = new NullCheckRegulation();
    private Regulation neRegulation = new EqualsRegulation(false);
    private Regulation notBlankRegulation = new NotBlankRegulation();
    private Regulation notNullWrapper = new NotNullWrapperRegulation();

    private ListBuffer<JCTree.JCStatement> thisStatements = CTreeUtil.newStatement();

    public MethodRegulationContext newStatement(String paramName) {
        thisStatements = CTreeUtil.newStatement();
        this.paramName = paramName;
        return this;
    }

    public void checkNotNull(JCTree.JCIdent field, JCTree.JCStatement action) {
        thisStatements = nullRegulation.handle(makerContext, thisStatements, paramName,
                field, makerContext.nullNode, action);
    }

    public void wrapper(JCTree.JCIdent field, JCTree.JCStatement action) {
        thisStatements = notNullWrapper.handle(makerContext, thisStatements, paramName,
                field, makerContext.nullNode, action);
    }

    public void checkNotBlank(JCTree.JCIdent field, JCTree.JCStatement action) {
        thisStatements = notBlankRegulation.handle(makerContext, thisStatements, paramName,
                field, makerContext.nullNode, action);
    }

    public void checkSize(Symbol symbol, byte type, JCTree.JCIdent field, JCTree.JCStatement action) {
        Regulation regulation = RegulationBuilder.buildSize(symbol, type);
        if (null == regulation) {
            return;
        }
        thisStatements = regulation.handle(makerContext, thisStatements, paramName,
                field, makerContext.nullNode, action);
    }

    public void checkValid(JCTree.JCExpressionStatement assign, JCTree.JCIdent field, JCTree.JCStatement action) {
        thisStatements.append(assign);
        thisStatements = neRegulation.handle(makerContext, thisStatements, paramName,
                field, makerContext.nullNode, action);
    }

    public JCTree.JCStatement create() {
        return makerContext.getTreeMaker().Block(0, thisStatements.toList());
    }
}
