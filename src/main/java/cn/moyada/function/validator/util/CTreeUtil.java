package cn.moyada.function.validator.util;

import com.sun.tools.javac.code.Symbol;

/**
 * 语法树工具
 * @author xueyikang
 * @since 1.0
 **/
public interface CTreeUtil {

    /**
     * 获取实际类型名
     * @param symbol
     * @return
     */
    static String getOriginalTypeName(Symbol symbol) {
        return symbol.asType().getOriginalType().toString();
    }
}
