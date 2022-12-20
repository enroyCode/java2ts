package org.enroy.java2ts.sampler.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.enroy.java2ts.sampler.domain.PageParam;

/**
 * enroy
 */
@Getter
@Setter
public class TestReq extends PageParam<String> {
    @Deprecated
    @ApiModelProperty("代码等于")
    private String codeEq;

    private String name;

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
