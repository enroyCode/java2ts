package io.gitee.enroy.java2ts.core.commons;

import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author zhuchao
 */
public class ClassUtil {

    /**
     * 获取范型参数的类型
     */
    public static Class<?> getTypeClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return getTypeClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            return getTypeClass(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return null;
        }
        if (type instanceof WildcardType) {
            return null;
        }
        return null;
    }

    /**
     * 是否为数字类型
     */
    public static boolean isNumber(Class<?> cls) {
        return cls.equals(Byte.class) || cls.equals(byte.class) || cls.equals(Integer.class) || cls.equals(
                int.class) || cls.equals(Short.class) || cls.equals(short.class) || cls.equals(long.class) || cls.equals(
                Long.class) || cls.equals(float.class) || cls.equals(Float.class) || cls.equals(double.class) || cls.equals(
                Double.class) || cls.equals(BigDecimal.class) || cls.equals(BigInteger.class) || cls.equals(Number.class);
    }

    /**
     * 是否为字符类型
     */
    public static boolean isString(Class<?> cls) {
        return cls.equals(String.class) || cls.equals(char.class) || cls.equals(Character.class);
    }

    /**
     * 是否为布尔类型
     */
    public static boolean isBoolean(Class<?> cls) {
        return cls.equals(boolean.class) || cls.equals(Boolean.class);
    }


    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // Do Nothing
        }
        return null;
    }


    /**
     * 递归回查被重写方法的指定注解。
     */
    public static <A extends Annotation> A getMethodAnnotationRecursion(Method method, Class<A> annotationClazz) {
        A anno = method.getAnnotation(annotationClazz);
        if (anno == null) {
            Class<?> declaringClass = method.getDeclaringClass();
            // 父类
            Class<?> superClass = declaringClass.getSuperclass();
            if (superClass != null) {
                Method overrideMethod = ClassUtil.getMethod(superClass, method.getName(), method.getParameterTypes());
                if (overrideMethod != null) {
                    return getMethodAnnotationRecursion(overrideMethod, annotationClazz);
                }
            }
            // 接口
            Class<?>[] superIns = declaringClass.getInterfaces();
            // TODO 暂只考虑一层接口实现
            for (Class<?> superInterface : superIns) {
                Method overrideMethod = ClassUtil.getMethod(superInterface, method.getName(), method.getParameterTypes());
                if (overrideMethod != null) {
                    return getMethodAnnotationRecursion(overrideMethod, annotationClazz);
                }
            }
        }
        return anno;
    }

    public static boolean isPresent(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassUtils.getDefaultClassLoader();
        }
        try {
            forName(className, classLoader);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static Class<?> forName(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader != null) {
            return classLoader.loadClass(className);
        }
        return Class.forName(className);
    }
}
