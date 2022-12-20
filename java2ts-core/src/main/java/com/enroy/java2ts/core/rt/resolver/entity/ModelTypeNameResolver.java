package com.enroy.java2ts.core.rt.resolver.entity;

import com.enroy.java2ts.core.rt.RuntimeProcessor;
import com.enroy.java2ts.core.rt.TypeProcessPool;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author zhuchao
 */
public class ModelTypeNameResolver {
    private final RuntimeProcessor config;

    public ModelTypeNameResolver(RuntimeProcessor config) {
        this.config = config;
    }

    /**
     * 获取类的类型名， 例如: Test< T extends Body>
     */
    public String resolveModelTypeName(Type type, TypeProcessPool collector) {
        if (type instanceof Class) {
            return resolveClassTypeName((Class<?>) type, collector);
        } else {
            throw new IllegalArgumentException("不支持的类型:" + type);
        }
    }

    private String resolveClassTypeName(Class<?> cls, TypeProcessPool collector) {
        StringBuilder name = new StringBuilder(config.getJavaTypeResolver().entityName(cls));
        TypeVariable<? extends Class<?>>[] variables = cls.getTypeParameters();// 范型形参
        if (ArrayUtils.isEmpty(variables)) {
            return name.toString();
        }
        name.append("<");
        for (TypeVariable<?> variable : variables) {
            name.append(variable.getName());
            if (variable.getBounds() != null && variable.getBounds().length > 1) {//获取范型变量的边界 例如： List<T extends Body>，返回的是Body
                name.append(" extends");
                for (Type type : variable.getBounds()) {
                    name.append(" ").append(config.getJavaTypeResolver().java2TsType(type, collector)).append(" &");
                }
                name = new StringBuilder(name.substring(0, name.length() - 2));
            }
            name.append(", ");
        }
        name = new StringBuilder(name.substring(0, name.length() - 2) + ">");
        return name.toString();
    }
}
