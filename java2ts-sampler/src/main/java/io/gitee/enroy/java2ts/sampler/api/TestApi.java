package io.gitee.enroy.java2ts.sampler.api;

import io.gitee.enroy.java2ts.sampler.domain.Res;
import io.gitee.enroy.java2ts.sampler.domain.TestDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

@Api("测试服务")
@RequestMapping("test")
public interface TestApi {
    @Deprecated
    @ApiOperation("空参数，返回Void泛型")
    @GetMapping("get/void")
    Res<Void> test1();

    @ApiOperation("带path参数请求用例")
    @GetMapping("get/path/{code}")
    Res<TestDto> test2(@ApiParam("代码") @PathVariable("code") String code, @ApiParam("uuid") @RequestParam("uuid") String uuid);

    @ApiOperation("测试3")
    @PostMapping("test3/body")
    Res<TestDto> test3(@ApiParam("请求体") @RequestBody TestReq req);

    @ApiOperation("测试3")
    @PostMapping("test3/withHeader")
    Res<TestDto> test4(@ApiParam("租户id") @RequestHeader("tenant") String tenant, @ApiParam("请求体") @RequestBody TestReq req);

    @ApiOperation("测试3")
    @PostMapping("test4/body")
    default Res<TestDto> test5(@ApiParam("请求体") @RequestBody TestReq req) {
        return null;
    }
}
