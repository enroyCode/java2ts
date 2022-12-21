package io.gitee.enroy.java2ts.core.rt.resolver.loader;

import io.gitee.enroy.java2ts.core.commons.ClassUtil;
import io.gitee.enroy.java2ts.core.entity.ApiEntity;
import io.gitee.enroy.java2ts.core.entity.ApiMethodEntity;
import io.gitee.enroy.java2ts.core.entity.TypeParameter;
import io.gitee.enroy.java2ts.core.rt.ClassMethod;
import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author chaos
 */
@Slf4j
public class ApiEntityLoadResolver {
    private final RuntimeProcessor config;

    public ApiEntityLoadResolver(RuntimeProcessor config) {
        this.config = config;
    }

    public List<ApiEntity> buildApiEntities(List<Class<?>> candidateApiClasses) {
        List<ApiEntity> apis = new ArrayList<>();
        for (Class<?> candidateApiClass : candidateApiClasses) {
            ApiEntity api = buildApiEntity(candidateApiClass);
            apis.add(api);
        }
        return apis;
    }

    private ApiEntity buildApiEntity(Class<?> apiClass) {
        ApiEntity apiEntity = new ApiEntity(apiClass);
        String entityName = config.getEntityNameResolver().buildEntityName(apiClass);
        apiEntity.setName(entityName);
        // 是否已过时
        Deprecated deprecated = AnnotationUtils.findAnnotation(apiClass, Deprecated.class);
        if (deprecated != null) {
            apiEntity.setDeprecated(true);
        }
        // path
        RequestMapping requestMappingAnn = AnnotationUtils.findAnnotation(apiClass, RequestMapping.class);
        // 获取public方法
        Method[] methods = apiClass.getDeclaredMethods();
        // 扫描父类
        // 1、获取所有public方法
        // 2、获取api注解
        Class<?> superCls = apiClass.getSuperclass();
        while (superCls != null && superCls != Object.class) {
            methods = ArrayUtils.addAll(methods, superCls.getDeclaredMethods());
            superCls = superCls.getSuperclass();
        }
        // 扫描接口
        // 接口
        Class<?>[] superIns = apiClass.getInterfaces();
        int temp = 0;
        // TODO 暂只考虑一层接口实现
        while (temp < superIns.length) {
            methods = ArrayUtils.addAll(methods, superIns[temp].getDeclaredMethods());
            temp++;
        }
        // 处理path
        String path = "";
        if (requestMappingAnn != null) {
            path = ArrayUtils.isNotEmpty(requestMappingAnn.value()) ? requestMappingAnn.value()[0] : "";
        }
        apiEntity.setPath(path.replaceAll("\\{", "\\$\\{"));// 前端取值语法为${}
        // 注释基于swagger注解
        Api api = AnnotationUtils.findAnnotation(apiClass, Api.class);
        if (api != null) {
            apiEntity.setNote(api.value());
        }
        List<ApiMethodEntity> apiMethodEntities = buildApiMethods(methods);
        apiEntity.setMethods(apiMethodEntities);
        return apiEntity;
    }

    private boolean ignoreMethod(Method method) {
        return Modifier.isStatic(method.getModifiers())// 不处理静态方法
                || !Modifier.isPublic(method.getModifiers())// 不处理静非public方法
                || Modifier.isAbstract(method.getModifiers()); // 不处理静抽象方法
    }

    private List<ApiMethodEntity> buildApiMethods(Method[] methods) {
        List<ApiMethodEntity> apiMethodEntities = new ArrayList<>();
        Set<ClassMethod> methodSet = new HashSet<>();
        for (Method method : methods) {
            if (ignoreMethod(method)) {
                continue;
            }
            ClassMethod classMethod = getClassMethod(method);
            // 判断重复方法则跳过
            if (methodSet.contains(classMethod)) {
                continue;
            }
            // 构造path
            Set<RequestMapping> requestMappings = AnnotatedElementUtils.findAllMergedAnnotations(method, RequestMapping.class);
            // 无RequestMapping注解的方法不处理
            if (CollectionUtils.isEmpty(requestMappings)) {
                log.warn("{} 未搜索到 @RequestMapping 相关注解，跳过处理", method);
                continue;
            }
            // RequestMapping注解数量超过1个不处理
            if (requestMappings.size() > 1) {
                log.warn("{} @RequestMapping 注解数量超过1个，跳过处理", method);
                continue;
            }
            requestMappings.stream().findFirst().ifPresent(requestMapping -> {
                ApiMethodEntity apiMethodEntity = new ApiMethodEntity();
                apiMethodEntity.setName(method.getName());// 方法名
                // 是否已弃用
                if (AnnotationUtils.findAnnotation(method, Deprecated.class) != null) {
                    apiMethodEntity.setDeprecated(true);
                }
                if (requestMapping.method().length != 1) {
                    log.warn("{} @RequestMapping.method 数量超过1个，跳过处理", method);
                    return;
                }
                // 设置请求方法
                apiMethodEntity.setRequestMethod(requestMapping.method()[0].name());
                // 设置请求路径
                apiMethodEntity.setPath(ArrayUtils.isNotEmpty(requestMapping.value()) ? requestMapping.value()[0] : "");
                // ts 占位符使用 ${}
                apiMethodEntity.setPath(apiMethodEntity.getPath().replaceAll("\\{", "\\$\\{"));
                // 构建注释
                buildNote(method, apiMethodEntity);
                // return
                apiMethodEntity.setRtnType(method.getGenericReturnType());
                // params
                String[] parameterNames = getParameterNames(method);// 形参方法名列表，当注解读不到的时候，使用形参变量名
                Annotation[][] annotationList = getMethodParameterAnnotations(method);// 注解列表
                Type[] types = method.getGenericParameterTypes();// 形参类型列表
                for (int i = 0; i < types.length; i++) {
                    Type type = types[i];
                    if (type instanceof Class) {
                        if (ignoreParameter((Class) type, method)) {
                            continue;
                        }
                    }
                    TypeParameter restParameter = new TypeParameter(); // 参数
                    Annotation[] annotations = annotationList[i];
                    String paramName = null;
                    try {
                        paramName = parameterNames[i];
                    } catch (Exception e) {
                        log.info("使用LocalVariableTableParameterNameDiscoverer获取方法<{}>参数名失败", method.getName());
                    }
                    int webAnnCount = 0;
                    for (Annotation annotation : annotations) {
                        Class<? extends Annotation> annotationType = annotation.annotationType();
                        if (annotationType == PathVariable.class) {
                            PathVariable pathVariable = (PathVariable) annotation;
                            paramName = StringUtils.isNotBlank(pathVariable.value()) ? pathVariable.value() : paramName;
                            apiMethodEntity.getPaths().add(restParameter);
                            restParameter.setRequired(true);
                            webAnnCount++;
                        } else if (annotationType == RequestHeader.class) {
                            RequestHeader requestHeader = (RequestHeader) annotation;
                            paramName = StringUtils.isNotBlank(requestHeader.value()) ? requestHeader.value() : paramName;
                            apiMethodEntity.getHeaders().add(restParameter);
                            restParameter.setRequired(requestHeader.required());
                            webAnnCount++;
                        } else if (annotationType == RequestParam.class) {
                            RequestParam requestParam = (RequestParam) annotation;
                            paramName = StringUtils.isNotBlank(requestParam.value()) ? requestParam.value() : paramName;
                            apiMethodEntity.getQueries().add(restParameter);
                            restParameter.setRequired(requestParam.required());
                            webAnnCount++;
                        } else if (annotationType == RequestBody.class) {
                            RequestBody requestBody = (RequestBody) annotation;
                            apiMethodEntity.setBody(restParameter);
                            if (paramName == null) {
                                paramName = "body";
                            }
                            restParameter.setRequired(requestBody.required());
                            webAnnCount++;
                        } else if (annotation.annotationType() == ApiParam.class) {// swagger注解取备注
                            ApiParam param = (ApiParam) annotation;
                            restParameter.setNote(param.value());
                        } else if (annotation.annotationType() == Deprecated.class) {// 是否已过期
                            restParameter.setDeprecated(true);
                        }
                    }
                    if (webAnnCount == 0) {
                        // 什么注解都没有，认为是body （HttpServletResponse）
                        apiMethodEntity.setBody(restParameter);
                        if (paramName == null) {
                            paramName = "body";
                        }
                        restParameter.setRequired(true);
                    }
                    if (webAnnCount > 1) {
                        log.info("方法<{}>的参数<{}>只允许存在PathVariable、RequestHeader、RequestParam、RequestBody其中一种注解", method, type);
                        continue;
                    }
                    restParameter.setName(paramName);
                    restParameter.setType(type);
                    methodSet.add(classMethod);
                }
                apiMethodEntities.add(apiMethodEntity);
            });
        }
        return apiMethodEntities;
    }

    /**
     * 如果path、header、param没有指定value或name，spring会取该参数的名称。由于java编译后的class文件并没有保留原参数名，故通过此方法读取。
     */
    private String[] getParameterNames(Method method) {
        LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        return parameterNameDiscoverer.getParameterNames(method);
    }

    private ClassMethod getClassMethod(Method method) {
        // 判重
        Class<?>[] paramCls = method.getParameterTypes();
        String[] params = new String[paramCls.length];
        for (int pi = 0; pi < paramCls.length; pi++) {
            params[pi] = paramCls[pi].getName();
        }
        return new ClassMethod(method.getName(), params);
    }

    /**
     * 递归获取方法参数注解。
     * <p>
     * 只要获取到注解，则停止递归。
     * </p>
     */
    private Annotation[][] getMethodParameterAnnotations(Method method) {
        Annotation[][] annotationList = method.getParameterAnnotations();

        if (!hasParameterAnnotations(annotationList)) {
            Class<?> declaringClass = method.getDeclaringClass();
            // 父类
            Class<?> superClass = declaringClass.getSuperclass();
            if (superClass != null) {
                Method overrideMethod = ClassUtil.getMethod(superClass, method.getName(), method.getParameterTypes());
                if (overrideMethod != null) {
                    return getMethodParameterAnnotations(overrideMethod);
                }
            }
            // 接口
            Class<?>[] superIns = declaringClass.getInterfaces();
            // TODO 暂只考虑一层接口实现
            for (Class<?> superInterface : superIns) {
                Method overrideMethod = ClassUtil.getMethod(superInterface, method.getName(), method.getParameterTypes());
                if (overrideMethod != null) {
                    return getMethodParameterAnnotations(overrideMethod);
                }
            }
        }
        return annotationList;
    }

    private boolean hasParameterAnnotations(Annotation[][] annotationList) {
        boolean hasAnnotations = false;
        for (Annotation[] annotations : annotationList) {
            if (annotations.length > 0) {
                hasAnnotations = true;
                break;
            }
        }
        return hasAnnotations;
    }

    /**
     * 忽略的方法形参
     */
    private boolean ignoreParameter(Class<?> clazz, Method method) {
        if (ServletRequest.class.isAssignableFrom(clazz)) {
            log.info("方法<{}>参数<{}>忽略", method, clazz);
            return true;
        } else if (ServletResponse.class.isAssignableFrom(clazz)) {
            log.info("方法<{}>参数<{}>忽略", method, clazz);
            return true;
        }
        return false;
    }

    private void buildNote(Method method, ApiMethodEntity apiMethodEntity) {
        ApiOperation apiOperation = ClassUtil.getMethodAnnotationRecursion(method, ApiOperation.class);
        if (apiOperation != null) {
            String value = apiOperation.value();
            String notes = apiOperation.notes();
            String note = value;
            if (StringUtils.isNotBlank(notes)) {
                if (StringUtils.isNotBlank(note)) {
                    note += "\n";
                }
                note += notes;
            }
            apiMethodEntity.setNote(note);
        }
    }
}
