package io.gitee.enroy.java2ts.core.rt;

import io.gitee.enroy.java2ts.core.config.IfThenClassCondition;
import io.gitee.enroy.java2ts.core.config.Java2TsProperties;
import io.gitee.enroy.java2ts.core.config.RuntimeExProperties;
import io.gitee.enroy.java2ts.core.entity.ApiEntity;
import io.gitee.enroy.java2ts.core.rt.resolver.entity.DefaultValueResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.entity.EntityNameResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.entity.JavaTypeResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.entity.ModelTypeNameResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.file.ApiWriteResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.file.ModelWriteResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.file.OutputPathResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.file.WorkspaceResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.loader.ApiEntityLoadResolver;
import io.gitee.enroy.java2ts.core.rt.resolver.loader.ModelEntityLoadResolver;
import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author chaos
 */
@Data
public class RuntimeProcessor {
    private String baseScanPackage;
    private String outputPath;
    private String apiRootPath;
    private String modelRootPath;
    private String fileExtension;
    private String srcPath;
    private boolean semicolonEnd;
    private String apiClientFactoryName;
    private String apiClientInstanceMethod;
    private String promiseResolveReturn;
    private String nullableDeclare;

    private List<Predicate<Class<?>>> apiFilters;
    private List<IfThenClassCondition<String>> filePathRules;
    private List<IfThenClassCondition<String>> entityRenameRules;

    private WorkspaceResolver workspaceResolver;
    //    private TsFileResolver tsFileResolver;
    private OutputPathResolver outputPathResolver;
    private EntityNameResolver entityNameResolver;
    private ApiEntityLoadResolver apiEntityLoadResolver;
    private JavaTypeResolver javaTypeResolver;
    private DefaultValueResolver defaultValueResolver;
    private ModelTypeNameResolver modelTypeNameResolver;
    private ApiWriteResolver apiWriteResolver;
    private ModelWriteResolver modelWriteResolver;
    private ModelEntityLoadResolver modelEntityLoadResolver;

    public RuntimeProcessor(Java2TsProperties properties, RuntimeExProperties exProperties) {
        this.baseScanPackage = properties.getBaseScanPackage();
        this.outputPath = properties.getOutputPath();
        this.apiRootPath = properties.getApiRootPath();
        this.modelRootPath = properties.getModelRootPath();
        this.fileExtension = properties.getFileExtension();
        this.srcPath = properties.getSrcPath();
        this.semicolonEnd = properties.isSemicolonEnd();
        this.apiClientFactoryName = properties.getApiClientFactoryName();
        this.apiClientInstanceMethod = properties.getApiClientInstanceMethod();
        this.promiseResolveReturn = properties.getPromiseResolveReturn();
        this.nullableDeclare = properties.getNullableDeclare();

        this.apiFilters = exProperties.getApiFilters();
        this.filePathRules = exProperties.getFilePathRules();
        this.entityRenameRules = exProperties.getEntityRenameRules();

        this.workspaceResolver = new WorkspaceResolver(this);
        this.apiEntityLoadResolver = new ApiEntityLoadResolver(this);
        this.entityNameResolver = new EntityNameResolver(this);
        this.outputPathResolver = new OutputPathResolver(this);
        this.javaTypeResolver = new JavaTypeResolver(this);
        this.defaultValueResolver = new DefaultValueResolver(this);
        this.modelTypeNameResolver = new ModelTypeNameResolver(this);
        this.apiWriteResolver = new ApiWriteResolver(this);
        this.modelWriteResolver = new ModelWriteResolver(this);
        this.modelEntityLoadResolver = new ModelEntityLoadResolver(this);
    }


    public File process(List<Class<?>> apiClasses) throws Exception {
        // ??????????????????????????????
        File root = workspaceResolver.initOutPutRoot();
        // apiClasses ?????????????????? ApiEntity
        List<ApiEntity> apis = apiEntityLoadResolver.buildApiEntities(apiClasses);
        // ??????api???????????????????????????????????? ModelEntity ??????
        TypeProcessPool typeProcessPool = apiWriteResolver.writeApisAndFindModels(apis, root);
        // ??????model??????
        modelWriteResolver.writeModels(typeProcessPool, root);
        return root;
    }

}
