package org.enroy.java2ts.sampler.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("统一响应体")
public class Res<T> {
  @ApiModelProperty("响应体内容")
  private T data;
  @ApiModelProperty("响应是否成功")
  private boolean success;
  @ApiModelProperty("错误提示信息")
  private String message;
}
