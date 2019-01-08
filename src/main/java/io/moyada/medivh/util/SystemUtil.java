package io.moyada.medivh.util;

import io.moyada.medivh.support.ElementOptions;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 系统参数工具
 * @author xueyikang
 * @since 1.0
 **/
public final class SystemUtil {

    private SystemUtil() {
    }

    /**
     * 获取参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 返回参数值
     */
    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (null == value) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * 解析类名和方法
     * @param methodInfo 方法信息
     * @param defaultValue 默认值
     * @return 类与方法的数组
     */
    public static String[] getClassAndMethod(String methodInfo, String[] defaultValue) {
        if (null == methodInfo) {
            return defaultValue;
        }
        int index = methodInfo.lastIndexOf(".");
        if (index < 1) {
            return defaultValue;
        }

        String clazz = methodInfo.substring(0, index);
        String method = methodInfo.substring(index + 1);
        return new String[]{clazz, method};
    }

    /**
     * 解析类路径包名
     * @param className 类名
     * @return 包名
     */
    public static String getPackage(String className) {
        int index = className.lastIndexOf(".");
        if (index < 0) {
            return "";
        }

        return className.substring(0, index);
    }

    /**
     * 创建工具类
     * @param filer 文件创建器
     * @param name 文见名
     * @throws IOException 资源获取失败异常
     */
    public static void createFile(Filer filer, String name) throws IOException {
        if (!ElementOptions.BLANK_METHOD[0].equals(ElementOptions.DEFAULT_BLANK_METHOD[0])) {
            return;
        }

        String content = getUtilContent("META-INF/Util.rs");
        String packageUrl = getPackage(name);

        JavaFileObject classFile = filer.createSourceFile(name);
        Writer writer = classFile.openWriter();
        writer.append("package ").append(packageUrl).append(";\n\n");
        writer.append(content);
        writer.flush();
        writer.close();
    }

    /**
     * 获取 jar 包内容
     * @param url 包链接
     * @return 返回内容信息
     * @throws IOException 资源获取失败异常
     */
    private static String getUtilContent(String url) throws IOException {
        URL resource = StringUtil.class.getResource("");
        String path = resource.getPath();
        String protocol = "file:";
        int i = path.lastIndexOf("!");
        path = path.substring(protocol.length(), i);

        JarFile jarFile = new JarFile(path);
        ZipEntry entry = jarFile.getEntry(url);
        if (null == entry) {
            throw new FileNotFoundException(url + " not exist.");
        }

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
}
