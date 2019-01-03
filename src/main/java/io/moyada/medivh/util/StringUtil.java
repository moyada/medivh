package io.moyada.medivh.util;

import io.moyada.medivh.core.Element;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 校验工具资源
 * @author xueyikang
 * @since 1.0
 **/
public class StringUtil {

    private StringUtil() {
    }

    /**
     * 创建工具类
     * @param filer
     * @param name
     * @throws IOException
     */
    public static void createFile(Filer filer, String name) throws IOException {
        if (!Element.BLANK_METHOD[0].equals(StringUtil.class.getName())) {
            return;
        }

        String content = getUtilContent("META-INF/Medivh.rs");
        String packageUrl = SystemUtil.getPackage(name);

        JavaFileObject classFile = filer.createSourceFile(name);
        Writer writer = classFile.openWriter();
        writer.append("package ").append(packageUrl).append(";\n\n");
        writer.append(content);
        writer.flush();
        writer.close();
    }

    /**
     * 获取 jar 包内容
     * @param url
     * @return
     * @throws IOException
     */
    private static String getUtilContent(String url) throws IOException {
        URL resource = StringUtil.class.getResource("");
        String path = resource.getPath();
        String protocol = "file:";
        int i = path.lastIndexOf("!");
        path = path.substring(protocol.length(), i);

        JarFile jarFile = new JarFile(path);
        ZipEntry entry = jarFile.getEntry(url);
        InputStream inputStream = jarFile.getInputStream(entry);
        StringBuilder content = new StringBuilder(inputStream.available());

        byte[] bytes = new byte[1024];
        int read;
        while ((read = inputStream.read(bytes)) > 0) {
            content.append(new String(bytes, 0, read));
        }
        inputStream.close();
        return content.toString();
    }

    /**
     * 规则名称，包含非法字符则返回 null
     * @param name
     * @return
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
     * @param ch
     * @return
     */
    private static boolean isDigital(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * 是否是字母
     * @param ch
     * @return
     */
    private static boolean isLetter(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    /**
     * 是否合法字符
     * @param ch
     * @return
     */
    private static boolean isRule(char ch) {
        return '_' == ch || ch == '$';
    }

    /**
     * 工具方法
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
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
}
