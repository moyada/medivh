package io.moyada.medivh.util;

/**
 * 校验工具资源
 * @author xueyikang
 * @since 1.0
 **/
public final class StringUtil {

    private StringUtil() {
    }

    static String trim(String str) {
        if (null == str) {
            return null;
        }
        str = str.trim();
        int length = str.length();
        if (0 == length) {
            return null;
        }
        return str;
    }

    public static boolean isEmpty(CharSequence str) {
        if (null == str) {
            return true;
        }
        int length = str.length();
        if (length == 0) {
            return true;
        }
        char ch;
        for (int i = 0; i < length; i++) {
            ch = str.charAt(i);
            if (ch != ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * 规则名称，包含非法字符则返回 null
     * @param name 名称
     * @return 规则名称
     */
    public static String fixName(String name) {
        int length = name.length();
        char ch;
        for (int i = 0; i < length; i++) {
            ch = name.charAt(i);
            if (isDigital(ch) || isLetter(ch) || isRule(ch)) {
                continue;
            }
            return null;
        }

        char first = name.charAt(0);
        if ('0' <= first && first <= '9') {
            name = "_" + name;
        }
        return name;
    }

    /**
     * 是否是数字
     * @param ch 字符
     * @return 是则返回 true
     */
    static boolean isDigital(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * 是否是字母
     * @param ch 字符
     * @return 是则返回 true
     */
    private static boolean isLetter(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    /**
     * 是否合法字符
     * @param ch 字符
     * @return 是则返回 true
     */
    private static boolean isRule(char ch) {
        return '_' == ch || ch == '$';
    }
}
