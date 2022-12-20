package com.enroy.java2ts.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * api方法抽象模型
 */
@Setter
@Getter
public class ApiMethodEntity implements Comparable<ApiMethodEntity> {
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";

    /**
     * 地址
     */
    private String path;
    /**
     * 请求方式
     */
    private String requestMethod;
    /**
     * 方法名
     */
    private String name;
    /**
     * path参数
     */
    private List<TypeParameter> paths = new ArrayList<>();
    /**
     * header参数
     */
    private List<TypeParameter> headers = new ArrayList<>();
    /**
     * query参数
     */
    private List<TypeParameter> queries = new ArrayList<>();
    /**
     * body
     */
    private TypeParameter body;
    /**
     * 返回值
     */
    private Type rtnType;

    /**
     * 注释
     */
    private String note;
    /**
     * 是否废弃
     */
    private boolean deprecated = Boolean.FALSE;

    @Override
    public int compareTo(ApiMethodEntity o) {
        // 按名称排序
        return name.compareTo(o.name);
    }

    /**
     * http请求中，post，put请求允许携带body
     */
    public boolean mayHasBody() {
        return requestMethod.equals(METHOD_POST) || requestMethod.equals(METHOD_PUT);
    }
}
