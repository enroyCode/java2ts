package io.gitee.enroy.java2ts.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;

@Getter
@Setter
public class TypeParameter implements Comparable<TypeParameter> {
    /**
     * 参数类型
     */
    private Type type;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 注释
     */
    private String note;
    /**
     * 是否必传
     */
    private boolean required;
    /**
     * 默认值
     */
    private Object dft;
    /**
     * 是否废弃
     */
    private boolean deprecated = Boolean.FALSE;

    @Override
    public int compareTo(TypeParameter o) {
        if (required == o.required) {
            return name.compareTo(o.name);
        }
        if (!required) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "TypeParameter{" +
                "name='" + name + '\'' +
                '}';
    }
}
