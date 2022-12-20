package org.enroy.java2ts.sampler.domain;

import lombok.Data;

/**
 * @author chaos
 */
@Data
public class PageParam<T> {
    private T idEq;
    private int page;
    private int pageSize;
}
