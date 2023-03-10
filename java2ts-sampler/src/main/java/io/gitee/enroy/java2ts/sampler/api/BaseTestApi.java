package io.gitee.enroy.java2ts.sampler.api;

import io.gitee.enroy.java2ts.sampler.domain.PageParam;
import io.gitee.enroy.java2ts.sampler.domain.Res;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author chaos
 */
public interface BaseTestApi<I extends PageParam, R> {
    @ApiOperation("测试1")
    @PostMapping("post/query")
    Res<List<R>> query(@ApiParam("请求体") @RequestBody I param);
}
