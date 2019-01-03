package io.moyada.medivh.util;

/**
 * 系统参数工具
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
     * 解析类名和方法
     * @param methodInfo
     * @param defaultValue
     * @return
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
     * @param className
     * @return
     */
    public static String getPackage(String className) {
        int index = className.lastIndexOf(".");
        if (index < 0) {
            return "";
        }

        return className.substring(0, index);
    }
}
