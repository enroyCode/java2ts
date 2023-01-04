package io.gitee.enroy.java2ts.core.rt.resolver.entity;

import io.gitee.enroy.java2ts.core.commons.ClassUtil;
import io.gitee.enroy.java2ts.core.commons.Consts;
import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import org.apache.commons.lang3.StringUtils;

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

    public String defaultValue(Class<?> cls, Object defaultValue) {
        if (cls == null) {
            return null;
        }
        if (defaultValue != null && !defaultValue.getClass().isAssignableFrom(cls)) {
            defaultValue = null;
        }
        if (cls.isArray() && cls.isAssignableFrom(Collection.class)) {
            return "[]";
        }
        if (ClassUtil.isString(cls)) {
            return defaultValue == null ? "''" : ("'" + defaultValue + "'");
        } else if (ClassUtil.isNumber(cls)) {
            return defaultValue == null ? "0" : defaultValue.toString();
        } else if (ClassUtil.isBoolean(cls)) {
            return defaultValue == null ? "false" : defaultValue.toString();
        }
        String name = config.getJavaTypeResolver().entityName(cls);
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if ("any".equals(name) || "void".equals(name)) {
            return null;
        }
        if (cls.isEnum()) {
            return defaultValue == null ? null : name + Consts.PERIOD + defaultValue;
        }
        return "new " + name + "()";
    }
}
