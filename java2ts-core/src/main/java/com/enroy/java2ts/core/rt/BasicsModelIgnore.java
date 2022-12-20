package com.enroy.java2ts.core.rt;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 属于指定的类型和指定的基类的model不处理
 *
 * @author zhuchao
 */
public class BasicsModelIgnore {
    private static final Set<Class<?>> classes = new HashSet<>();
    private static final Set<Class<?>> superClass = new HashSet<>();

    static {
        classes.add(Object.class);
        classes.add(void.class);
        classes.add(Void.class);
        classes.add(String.class);
        classes.add(byte.class);
        classes.add(short.class);
        classes.add(int.class);
        classes.add(long.class);
        classes.add(float.class);
        classes.add(double.class);
        classes.add(char.class);
        classes.add(boolean.class);
        classes.add(Byte.class);
        classes.add(Short.class);
        classes.add(Integer.class);
        classes.add(Long.class);
        classes.add(Float.class);
        classes.add(Double.class);
        classes.add(Character.class);
        classes.add(Boolean.class);
        classes.add(BigDecimal.class);
        classes.add(BigInteger.class);
        classes.add(Number.class);
        classes.add(Date.class);
        //
        superClass.add(Map.class);
        superClass.add(Collection.class);
    }

    public static boolean ignore(Type type) {
        if (type instanceof Class) {
            return ignore((Class<?>) type);
        }
        return true;
    }

    private static boolean ignore(Class<?> cls) {
        if (cls == null) {
            return true;
        }
        if (cls.isInterface() || cls.isAnnotation()) {//不处理接口以及注解类
            return true;
        }
        if (classes.contains(cls)) {//不处理基础类型
            return true;
        }
        for (Class<?> clazz : superClass) {//不处理Map和Collection类
            if (clazz.isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }
}
