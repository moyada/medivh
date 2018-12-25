package cn.moyada.method.validator.util;

import com.sun.tools.javac.main.JavaCompiler;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xueyikang
 * @since 1.0
 **/
public final class ClassUtil {

    public final static byte VERSION;
    public final static byte OLD_VERSION = 0;
    public final static byte JAVA8_VERSION = 1;

    static {
        String version = JavaCompiler.version();
        if (version.startsWith("1.6") || version.startsWith("1.7")) {
            VERSION = OLD_VERSION;
        } else if (version.startsWith("1.8")) {
            VERSION = JAVA8_VERSION;
        } else {
            VERSION = JAVA8_VERSION;
//            throw new UnsupportedClassVersionError("Unsupported java compiler version" + version);
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
