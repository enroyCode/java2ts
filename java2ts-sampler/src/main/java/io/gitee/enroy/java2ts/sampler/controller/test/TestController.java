package io.gitee.enroy.java2ts.sampler.controller.test;

import io.gitee.enroy.java2ts.sampler.api.TestApi;
import io.gitee.enroy.java2ts.sampler.api.TestReq;
import io.gitee.enroy.java2ts.sampler.domain.Res;
import io.gitee.enroy.java2ts.sampler.domain.TestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController implements TestApi {

    public static void main(String[] args) {
        log.info("---AnnotationUtils---");
        log.info(" getAnnotation @RequestMapping: " + AnnotationUtils.getAnnotation(TestController.class,
                RequestMapping.class));
        log.info(" findAnnotation @RequestMapping: " + AnnotationUtils.findAnnotation(TestController.class,
                RequestMapping.class));
        log.info("---AnnotatedElementUtils---");
        log.info("ParentController getMergedAnnotation @RequestMapping: " + AnnotatedElementUtils.getMergedAnnotation(
                TestController.class, RequestMapping.class));
        log.info("ChildController findMergedAnnotation @RequestMapping: " + AnnotatedElementUtils.findMergedAnnotation(
                TestController.class, RequestMapping.class));
    }

    @Override
    public Res<Void> test1() {
        return null;
    }

    @Override
    public Res<TestDto> test2(String code, String uuid) {
        return null;
    }

    @Override
    public Res<TestDto> test3(TestReq req) {
        return null;
    }

    @Override
    public Res<TestDto> test5(TestReq req) {
        return null;
    }

    @Override
    public Res<TestDto> test4(String tenant, TestReq req) {
        return null;
    }
}
