package io.gitee.enroy.java2ts.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * api模型抽象对象
 */
@Getter
@Setter
public class ApiEntity extends Java2TsEntity {
    /**
     * 路径
     */
    private String path;
    /**
     * 方法
     */
    private List<ApiMethodEntity> methods = new ArrayList<>();

    public ApiEntity(Type type) {
        super(type);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.fullName = name;
    }
}
