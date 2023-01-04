package io.gitee.enroy.java2ts.core.rt.resolver.entity;

import io.gitee.enroy.java2ts.core.commons.ClassUtil;
import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import io.gitee.enroy.java2ts.core.rt.TypeProcessPool;

import java.lang.reflect.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 与泛型（参数化类型）有关，代表不能被归一到Class类中的类型但是又和原始类型齐名的类型：
 * ParameterizedType：参数化类型，例如：List<String> 、List<T>、List<? extends T>。
 * GenericArrayType：泛型数组类型，例如T[]。
 * WildcardType：泛型限定的的参数化类型 (含通配符+通配符限定表达式)，例如：?、? super T、? extends T。
 * TypeVariable：类型变量(范型)，描述类型，例如：T、E、S等
 *
 * @author chaos
 */
public class JavaTypeResolver {
    private final RuntimeProcessor config;

    public JavaTypeResolver(RuntimeProcessor config) {
        this.config = config;
    }

    public String java2TsType(Type type, TypeProcessPool importsClassPool) {
        if (type == null) {
            return "any";
        }
        if (type instanceof Class) {//普通类型
            Class<?> cls = (Class<?>) type;
            if (cls.isArray()) {//处理数组
                String tsType = java2TsType(cls.getComponentType(), importsClassPool);
                return tsType + "[]";
            }
            importsClassPool.add(cls);
            return entityName(cls);
        }
        // 处理范型
        if (type instanceof ParameterizedType) {// 参数化类型。
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();//获取参数化类型的类型，例如List<String> => List
            Type[] typeArguments = parameterizedType.getActualTypeArguments();//获取参数化类型的参数 例如List<String> => String
            if (rawType == List.class) {//List类型的范型
                String tsType = java2TsType(typeArguments[0], importsClassPool);
                return tsType + "[]";
            }
            String rawTypeStr = java2TsType(rawType, importsClassPool);
            if ("any".equals(rawTypeStr)) {// 如果参数化类型为any，则不再继续解析参数化类型中的参数
                return rawTypeStr;
            }
            StringBuilder tsType = new StringBuilder(rawTypeStr);
            tsType.append("<");
            for (Type t : typeArguments) {
                String r = java2TsType(t, importsClassPool);
                if (r == null) {
                    continue;
                }
                tsType.append(r).append(", ");
            }
            if (tsType.toString().endsWith("<")) {
                tsType = new StringBuilder(tsType.substring(0, tsType.length() - 1));//去除"<"
            } else {
                tsType = new StringBuilder(tsType.substring(0, tsType.length() - 2));//去除", "
                tsType.append(">");
            }
            return tsType.toString();
        } else if (type instanceof GenericArrayType) {//参数化类型数组
            GenericArrayType arrayType = (GenericArrayType) type;
            Type componentType = arrayType.getGenericComponentType();// 脱去第一层<>后剩余部分
            String tsType = java2TsType(componentType, importsClassPool);
            return tsType + "[]";
        } else if (type instanceof WildcardType) {// 通配符
            return "any";
        } else if (type instanceof TypeVariable) {//表示类型参数或者又叫做类型变量
            return ((TypeVariable<?>) type).getName();
        }

        return "any";
    }


    /**
     * java class类型 转ts class类型
     * 简单类型转换，不处理泛型等其他参数化类型
     *
     * @param type java 类型
     * @return ts 类型
     */
    public String entityName(Type type) {
        // 基础数据类型
        if (type == null) {
            return null;
        }
        if (type instanceof Class) {
            return entityName((Class<?>) type);
        }
        // 普通类对象
        throw new IllegalArgumentException("不支持的类型:" + type);
    }


    private String entityName(Class<?> cls) {
        if (ClassUtil.isString(cls)) {
            return "string";
        } else if (ClassUtil.isNumber(cls)) {
            return "number";
        } else if (ClassUtil.isBoolean(cls)) {
            return "boolean";
        } else if (cls.equals(Object.class) || Map.class.isAssignableFrom(cls)) {
            return "any";
        } else if (cls.equals(Date.class)) {
            return "Date";
        } else if (cls.equals(Void.class) || cls.equals(void.class)) {
            return "void";
        }
        return config.getEntityNameResolver().buildEntityName(cls);
    }
}
