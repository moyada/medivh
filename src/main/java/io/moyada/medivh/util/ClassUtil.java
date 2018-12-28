package io.moyada.medivh.util;

import com.sun.tools.javac.main.JavaCompiler;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 类相关工具
 * @author xueyikang
 * @since 1.0
 **/
public final class ClassUtil {

    final static byte VERSION;
    final static byte VERSION_6 = 0;
    final static byte VERSION_7 = 1;
    final static byte VERSION_8 = 2;

    /**
     * 获取编译器版本号
     */
    static {
        String version = JavaCompiler.version();
        if (null == version) {
            throw new UnknownError("Can not find available java compiler version.");
        }
        int length = version.length();
        if (length == 0) {
            throw new UnknownError("Can not find available java compiler version.");
        }

        int sverion;
        // 1.xxx
        if (length > 1 && version.charAt(1) == '.') {
            if (length < 3) {
                throw new UnknownError("Unknown java compiler version " + version);
            }

            // 1.x
            sverion = getFirstNumber(version, 2);

            // 1.xx
            if (length > 3) {
                int number = getIndexNumber(version, 3);
                if (number >= 0) {
                    sverion *= 10;
                    sverion += number;
                }
            }
        } else {
            // x
            sverion = getFirstNumber(version, 0);

            // 1x
            if (sverion < 2 && length > 2) {
                int number = getIndexNumber(version, 1);
                if (number >= 0) {
                    sverion *= 10;
                    sverion += number;
                }
            }
        }

        VERSION = getSpecialVersion(sverion, version);
    }

    /**
     * 获取版本号第一位有效数字，非数字则抛出异常
     * @param version
     * @param index
     * @return
     */
    private static int getFirstNumber(String version, int index) {
        int number = getIndexNumber(version, index);
        if (number == -1) {
            throw new UnknownError("Unknown java compiler version " + version);
        }
        return number;
    }

    /**
     * 获取版本号下标数字
     * @param version
     * @param index
     * @return
     */
    private static int getIndexNumber(String version, int index) {
        char v = version.charAt(index);
        if (v >= '0' && v <= '9') {
            return Character.digit(v, 10);
        } else {
            return -1;
        }
    }

    /**
     * 检查版本
     * @param v
     * @param version
     * @return
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
            default:
                return VERSION_8;
        }
    }

    private static Map<String, SoftReference<Class<?>>> classMap = new HashMap<String, SoftReference<Class<?>>>();
    private static Map<String, SoftReference<Method>> methodMap = new HashMap<String, SoftReference<Method>>();
    private static Map<String, SoftReference<Field>> fieldMap = new HashMap<String, SoftReference<Field>>();

    private static <K, T> T getReference(Map<K, SoftReference<T>> refMap, K key) {
        SoftReference<T> ref = refMap.get(key);
        if (null == ref) {
            return null;
        }
        return ref.get();
    }

    private static <K, T> void putReference(Map<K, SoftReference<T>> refMap, K key, T value) {
        refMap.put(key, new SoftReference<T>(value));
    }

    /**
     * 获取类对象
     * @param className
     * @return
     */
    public final static Class<?> getClass(String className) {
        Class<?> clazz = getReference(classMap, className);
        if (null != clazz) {
            return clazz;
        }
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        putReference(classMap, className, clazz);
        return clazz;
    }

    /**
     * 获取类下方法
     * @param clazz
     * @param name
     * @param parameterTypes
     * @return
     */
    public final static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = getReference(methodMap, name);
        if (null != method) {
            return method;
        }

        try {
            method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        putReference(methodMap, name, method);
        return method;
    }

    public final static <T> T invoke(Method method, Object target, Object... args) {
        try {
            @SuppressWarnings("unchecked")
            T m = (T) method.invoke(target, args);
            return m;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取静态属性
     * @param clazz
     * @param file
     * @return
     */
    public final static Object getStaticField(Class<?> clazz, String file) {
        return getField(clazz, null, file);
    }

    /**
     * 获取对象属性
     * @param clazz
     * @param target
     * @param file
     * @return
     */
    public final static Object getField(Class<?> clazz, Object target, String file) {
        String fileName = clazz.getName() + file;
        Field field = getReference(fieldMap, fileName);
        if (null != field) {
            return getValue(field, target);
        }

        try {
            field = clazz.getDeclaredField(file);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        putReference(fieldMap, fileName, field);
        return getValue(field, target);
    }

    private final static Object getValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
