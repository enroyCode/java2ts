package org.enroy.java2ts.starter;

import org.enroy.java2ts.core.commons.Consts;
import org.enroy.java2ts.core.config.Java2TsProperties;
import org.enroy.java2ts.core.config.RuntimeExProperties;
import org.enroy.java2ts.core.rt.resolver.loader.ApiScanResolver;
import org.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(value = Java2TsProperties.ENABLE, havingValue = "true")
@ComponentScan(Consts.CONVERTER_CONTROLLER)
public class Java2tsAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = Java2TsProperties.PREFIX)
    public Java2TsProperties java2TsProperties() {
        return new Java2TsProperties();
    }

    @Bean
    public ApiScanResolver apiScanProcessor() {
        return new ApiScanResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RuntimeExProperties runtimeExConfig() {
        return RuntimeExProperties.builder().build();
    }

    /**
     * 生成运行时配置，若需要特殊处理，请执行定义
     */
    @Bean
    public RuntimeProcessor runtimeConfigProcessor(Java2TsProperties java2TsProperties, RuntimeExProperties exProperties) {
        return new RuntimeProcessor(java2TsProperties, exProperties);
    }
}
