package org.enroy.java2ts.core.rt.resolver.entity;

import org.apache.commons.lang3.StringUtils;
import org.enroy.java2ts.core.commons.ClassUtil;
import org.enroy.java2ts.core.rt.RuntimeProcessor;

import java.util.Collection;

/**
 * 默认值处理器
 *
 * @author zhuchao
 */
public class DefaultValueResolver {
    private final RuntimeProcessor config;

    public DefaultValueResolver(RuntimeProcessor config) {
        this.config = config;
    }

    public  String defaultValue(Class<?> cls, Object value) {
        if (cls == null) {
            return null;
        }
        if (value != null && !value.getClass().isAssignableFrom(cls)) {
            value = null;
        }
        if (cls.isArray() && cls.isAssignableFrom(Collection.class)) {
            return "[]";
        }
        if (ClassUtil.isString(cls)) {
            return value == null ? "''" : ("'" + value + "'");
        } else if (ClassUtil.isNumber(cls)) {
            return value == null ? "0" : value.toString();
        } else if (ClassUtil.isBoolean(cls)) {
            return value == null ? "false" : value.toString();
        }
        if (cls.isEnum()) {
            return value == null ? null : value.toString();
        }
        String name = config.getJavaTypeResolver().entityName(cls);
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if ("any".equals(name) || "void".equals(name)) {
            return null;
        }
        return "new " + name + "()";
    }
}
