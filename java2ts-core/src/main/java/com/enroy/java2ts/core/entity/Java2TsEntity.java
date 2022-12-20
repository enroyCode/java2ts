package com.enroy.java2ts.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * @author zhuchao
 */
@Setter
@Getter
public class Java2TsEntity {
    /**
     * 类型
     */
    protected Type type;
    /**
     * 实体名,文件名
     */
    protected String name;
    /**
     * 实体全名，包含泛型信息
     */
    protected String fullName;
    /**
     * 注释
     */
    protected String note;
    /**
     * 是否废弃
     */
    protected boolean deprecated = Boolean.FALSE;


    public Java2TsEntity(Type type) {
        this.type = type;
    }
}
