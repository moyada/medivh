package io.moyada.medivh.util;

import sun.misc.Unsafe;

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

    private ClassUtil() {
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
     * 关闭 JAVA9 使用 Unsafe 反射警告
     */
    public static void disableJava9SillyWarning() {
        if (Compiler.CURRENT_VERSION < Compiler.JAVA_9) {
            return;
        }
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Throwable t) {
            // ignore it
        }
    }

    /**
     * 获取类对象
     * @param className 类名称
     * @return 类对象
     */
    static Class<?> getClass(String className) {
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
     * @param clazz 类对象
     * @param name 方法米
     * @param parameterTypes 方法参数
     * @return 方法对象
     */
    static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
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
     * 方法调用
     * @param method 方法对象
     * @param target 调用对象
     * @param args 参数
     * @param <T> 返回类型
     * @return 返回调用数据
     */
    static <T> T invoke(Method method, Object target, Object... args) {
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
     * @param clazz 类对象
     * @param file 静态属性名
     * @return 属性对象
     */
    static Object getStaticField(Class<?> clazz, String file) {
        return getField(clazz, null, file);
    }

    /**
     * 获取对象属性
     * @param clazz 类对象
     * @param target 调用对象
     * @param file 属性名
     * @return 属性对象
     */
    static Object getField(Class<?> clazz, Object target, String file) {
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

    /**
     * 获取对象内数据
     * @param field 字段对象
     * @param target 调用对象
     * @return 字段数据
     */
    private static Object getValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
