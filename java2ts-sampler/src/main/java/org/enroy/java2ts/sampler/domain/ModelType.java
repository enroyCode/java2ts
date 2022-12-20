package org.enroy.java2ts.sampler.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zhuchao
 */
@Data
public class ModelType<T> {
    private T data01;
    private String data02;
    private int data03;
    private List<T> data04;
    private List<String> data05;
    private Map<String, T> data06;
    private T[] data07;
    // 上界通配符
    private List<? extends T> data08;
    // 下界通配符
    private List<? super T> data9;
    // 无界通配符
    private List<?> data10;

}
