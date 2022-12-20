package com.enroy.java2ts.sampler.config;

import com.enroy.java2ts.core.config.RuntimeExProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chaos
 */
@Configuration
public class Java2tsConfiguration {
    @Bean
    public RuntimeExProperties runtimeExConfig() {
        return RuntimeExProperties.builder()//
//                .apiFilter(TestController.class)//
                .modelPathRule((cls) -> {//
                    Package pkg = cls.getPackage();
                    String pkgStr = pkg == null ? "" : pkg.getName();
                    if (pkgStr.startsWith("org.enroy.java2ts.sampler")) {
                        return true;
                    }
                    return false;
                }, (cls) -> {
                    String pkgName = cls.getPackage().getName();
                    int index = pkgName.indexOf("controller");
                    if (index > -1) {
                        return pkgName.substring(index + "controller".length() + 1);
                    }
                    return pkgName.substring("org.enroy.java2ts.sampler".length());
                }) //
                .entityRenameSuffix("Controller", "Api")// 修改类以Controller结尾改为以Api结尾
                .build();
    }
}
