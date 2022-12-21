package io.gitee.enroy.java2ts.sampler.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel("测试类")
public class TestDto extends Entity {
    @ApiModelProperty("年龄")
    private BigDecimal age;
    @ApiModelProperty("不同模型类型")
    private ModelType<String> modelType;
    @ApiModelProperty("状态")
    private State state;
}
