package org.enroy.java2ts.core.rt;

import lombok.Data;
import org.enroy.java2ts.core.config.IfThenClassCondition;
import org.enroy.java2ts.core.config.Java2TsProperties;
import org.enroy.java2ts.core.config.RuntimeExProperties;
import org.enroy.java2ts.core.entity.ApiEntity;
import org.enroy.java2ts.core.rt.resolver.entity.JavaTypeResolver;
import org.enroy.java2ts.core.rt.resolver.entity.ModelTypeNameResolver;
import org.enroy.java2ts.core.rt.resolver.entity.DefaultValueResolver;
import org.enroy.java2ts.core.rt.resolver.entity.EntityNameResolver;
import org.enroy.java2ts.core.rt.resolver.file.OutputPathResolver;
import org.enroy.java2ts.core.rt.resolver.loader.ApiEntityLoadResolver;
import org.enroy.java2ts.core.rt.resolver.file.ApiWriteResolver;
import org.enroy.java2ts.core.rt.resolver.file.ModelWriteResolver;
import org.enroy.java2ts.core.rt.resolver.file.WorkspaceResolver;
import org.enroy.java2ts.core.rt.resolver.loader.ModelEntityLoadResolver;

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
        // 初始化输出文件根目录
        File root = workspaceResolver.initOutPutRoot();
        // apiClasses 转换成抽象的 ApiEntity
        List<ApiEntity> apis = apiEntityLoadResolver.buildApiEntities(apiClasses);
        // 写入api文件，在此过程中顺便采集 ModelEntity 模型
        TypeProcessPool typeProcessPool = apiWriteResolver.writeApisAndFindModels(apis, root);
        // 写入model文件
        modelWriteResolver.writeModels(typeProcessPool, root);
        return root;
    }

}
