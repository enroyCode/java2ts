package io.gitee.enroy.java2ts.core.rt.resolver.loader;

import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * api接口扫描器
 *
 * @author chaos
 */
public class ApiScanResolver implements ResourceLoaderAware, EnvironmentAware {
    @Autowired
    private RuntimeProcessor config;

    /**
     * 扫描指定包路径的api
     */
    public List<Class<?>> scanApis() {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>(scanner.findCandidateComponents(config.getBaseScanPackage()));
        List<Class<?>> apiClasses = new ArrayList<>();
        List<Predicate<Class<?>>> filters = apiFilters();
        for (BeanDefinition candidateComponent : candidateComponents) {
            AnnotatedBeanDefinition db = (AnnotatedBeanDefinition) candidateComponent;
            AnnotationMetadata annotationMetadata = db.getMetadata();
            String className = annotationMetadata.getClassName();
            Class<?> clazz = ClassUtils.resolveClassName(className, null);
            if (ignore(clazz, filters)) {
                continue;
            }
            apiClasses.add(clazz);
        }
        return apiClasses;
    }

    private boolean ignore(Class<?> clazz, List<Predicate<Class<?>>> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return false;
        }
        for (Predicate<Class<?>> filter : filters) {
            if (filter.test(clazz)) {
                return true;
            }
        }
        return true;
    }

    private List<Predicate<Class<?>>> apiFilters() {
        return config.getApiFilters();
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }
        };
    }


    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
