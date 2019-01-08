package io.moyada.medivh.util;

import com.sun.tools.javac.main.JavaCompiler;

/**
 * 版本信息工具
 * @author xueyikang
 * @since 1.2.2
 **/
class VersionUtil {

    private VersionUtil() {
    }

    final static byte VERSION;
    final static byte VERSION_6 = 0;
    final static byte VERSION_7 = 1;
    final static byte VERSION_8 = 2;
    final static byte VERSION_9 = 3;

    // 获取编译器版本号
    static {
        String version = JavaCompiler.version();
        int sversion = VersionUtil.getVersion(version);
        VERSION = getSpecialVersion(sversion, version);
    }

    /**
     * 检查版本
     * @param v 版本数字
     * @param version 版本信息
     * @return 版本标记
     */
    private static byte getSpecialVersion(int v, String version) {
        if (v < 6) {
            throw new UnsupportedClassVersionError("Unsupported java compiler version " + version);
        }

        switch (v) {
            case 6:
                return VERSION_6;
            case 7:
                return VERSION_7;
            case 8:
                return VERSION_8;
            default:
                return VERSION_9;
        }
    }

    /**
     * 解析编译器版本号
     * @param version 版本名称
     * @return 版本号
     */
    private static int getVersion(String version) {
        if (null == version) {
            throw new UnknownError("Can not find available java compiler version.");
        }
        int length = version.length();
        if (length == 0) {
            throw new UnknownError("Can not find available java compiler version.");
        }

        int sversion;
        // 1.xxx
        if (length > 1 && version.charAt(1) == '.') {
            if (length < 3) {
                throw new UnknownError("Unknown java compiler version " + version);
            }

            // 1.x
            sversion = getFirstNumber(version, 2);

            // 1.xx
            if (length > 3) {
                int number = getIndexNumber(version, 3);
                if (number >= 0) {
                    sversion *= 10;
                    sversion += number;
                }
            }
        } else {
            // x
            sversion = getFirstNumber(version, 0);

            // 1x
            if (sversion < 2 && length > 2) {
                int number = getIndexNumber(version, 1);
                if (number >= 0) {
                    sversion *= 10;
                    sversion += number;
                }
            }
        }

        return sversion;
    }

    /**
     * 获取版本号第一位有效数字，非数字则抛出异常
     * 非法数字则抛出 {@link UnknownError} 异常
     * @param version 版本信息
     * @param index 位数
     * @return 版本数字
     */
    private static int getFirstNumber(String version, int index) {
        int number = getIndexNumber(version, index);
        if (number == -1) {
            throw new UnknownError("Unknown java compiler version " + version);
        }
        return number;
    }

    /**
     * 获取版本信息下标数字
     * @param version 版本信息
     * @param index 下标
     * @return 版本数字
     */
    private static int getIndexNumber(String version, int index) {
        char v = version.charAt(index);
        if (v >= '0' && v <= '9') {
            return Character.digit(v, 10);
        } else {
            return -1;
        }
    }
}
