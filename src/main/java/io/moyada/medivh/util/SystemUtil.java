package io.moyada.medivh.util;

import io.moyada.medivh.annotation.Verify;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class SystemUtil {

    private SystemUtil() {
    }

    /**
     * 获取参数
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (null == value) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * 获取临时变量名
     * @param verify
     * @return
     */
    public static String getTmpVar(Verify verify) {
        String var = verify.var();
        if (var.isEmpty()) {
            return Element.LOCAL_VARIABLE;
        }
        var = var.trim();
        if (var.isEmpty()) {
            return Element.LOCAL_VARIABLE;
        }
        return var;
    }
}
