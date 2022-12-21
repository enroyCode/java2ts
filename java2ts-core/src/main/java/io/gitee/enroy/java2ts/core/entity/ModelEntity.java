package io.gitee.enroy.java2ts.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据模型抽象对象
 *
 * @author zhuchao
 */
@Setter
@Getter
public class ModelEntity extends Java2TsEntity {

    /**
     * 是否枚举型
     */
    private boolean isEnum;
    /**
     * 父类
     */
    private Type superType;
    /**
     * 成员变量
     */
    private List<TypeParameter> fields = new ArrayList<>();
    /**
     * 枚举型变量
     */
    private List<TypeParameter> enums = new ArrayList<>();

    public ModelEntity(Class<?> type) {
        super(type);
    }

}
