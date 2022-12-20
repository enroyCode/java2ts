package org.enroy.java2ts.core.rt.resolver.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.enroy.java2ts.core.commons.ClassUtil;
import org.enroy.java2ts.core.entity.ModelEntity;
import org.enroy.java2ts.core.entity.TypeParameter;
import org.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * @author chaos
 */
@Slf4j
public class ModelEntityLoadResolver {
    private final RuntimeProcessor config;

    public ModelEntityLoadResolver(RuntimeProcessor config) {
        this.config = config;
    }

    public ModelEntity buildModelEntity(Type type) {
        if (type == null) {
            return null;
        }
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            ModelEntity model = new ModelEntity(cls);
            model.setSuperType(getSupperClassExceptObject(cls));
            boolean isEnum = cls.isEnum();
            model.setEnum(isEnum);
            // 判断model是否废弃
            if (AnnotationUtils.findAnnotation(cls, Deprecated.class) != null) {
                model.setDeprecated(true);
            }
            //处理model的注释
            ApiModel apiModel = AnnotationUtils.findAnnotation(cls, ApiModel.class);
            if (apiModel != null) {
                model.setNote(apiModel.value());
            }
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                TypeParameter typeParameter = buildFromField(cls, field);
                if (typeParameter == null) {
                    continue;
                }
                if (isEnum) {
                    model.getEnums().add(typeParameter);
                } else {
                    model.getFields().add(typeParameter);
                }
            }
            return model;
        } else {
            return null;
        }
    }

    /**
     * 获取父类（参数化）
     */
    private Type getSupperClassExceptObject(Class<?> cls) {
        Type superclass = cls.getGenericSuperclass();
        if (superclass != null && !"java.lang.Object".equals(superclass.getTypeName())) {
            return superclass;
        }
        return null;
    }

    public TypeParameter buildFromField(Class<?> cls, Field field) {
        int mod = field.getModifiers();
        boolean isEnum = cls.isEnum();
        if (isEnum) {
            if (!Modifier.isStatic(mod) || field.getType() != cls) {
                return null;
            }
        } else if (Modifier.isStatic(mod)) {
            return null;
        }
        TypeParameter typeField = new TypeParameter();
        // 获取
        typeField.setType(field.getGenericType());
        typeField.setName(field.getName());
        Method readMethod = getReadMethod(cls, field);

        ApiModelProperty apiModelProperty = getAnnotationByFieldOrReadMethod(cls, field, readMethod, ApiModelProperty.class);
        if (apiModelProperty != null) {
            String value = apiModelProperty.value();
            String notes = apiModelProperty.notes();
            String note = value;
            if (StringUtils.isNotBlank(notes)) {
                if (StringUtils.isNotBlank(note)) {
                    note += "\n";
                }
                note += notes;
            }
            typeField.setNote(note);
            typeField.setRequired(apiModelProperty.required());
        }
        if (ClassUtil.isPresent("com.fasterxml.jackson.annotation.JsonProperty", null)) {
            JsonIgnore ignore = getAnnotationByFieldOrReadMethod(cls, field, readMethod, JsonIgnore.class);
            if (ignore != null) {
                return null;
            }
            JsonProperty jsonProperty = getAnnotationByFieldOrReadMethod(cls, field, readMethod, JsonProperty.class);
            if (jsonProperty != null) {
                typeField.setName(jsonProperty.value());
            }
        }
        if (field.getAnnotation(Deprecated.class) != null) {
            typeField.setDeprecated(true);
        }
        return typeField;
    }

    private Method getReadMethod(Class<?> cls, Field field) {
        if (!cls.isEnum()) {
            String readMethodName = (field.getType() == boolean.class ? "is" : "get") + first2Upper(field.getName());
            try {
                return cls.getMethod(readMethodName);
            } catch (NoSuchMethodException e) {
                log.info("找不到方法:{}", readMethodName);
            }
        }
        return null;
    }

    /**
     * 从字段或者read方法上获取指定注解
     */
    public <A extends Annotation> A getAnnotationByFieldOrReadMethod(Class<?> cls, Field field, Method readMethod, Class<A> annotationType) {
        A annotation = AnnotationUtils.getAnnotation(field, annotationType);
        if (annotation == null) {
            if (readMethod == null || cls.isEnum()) {
                return null;
            }
            annotation = AnnotationUtils.getAnnotation(readMethod, annotationType);
        }
        return annotation;
    }

    /**
     * 单词首字母大写
     */
    private String first2Upper(String name) {
        char[] cs = name.toCharArray();
        if (cs[0] >= 97 && cs[0] <= 122) {
            cs[0] -= 32;
        }
        return String.valueOf(cs);
    }
}
