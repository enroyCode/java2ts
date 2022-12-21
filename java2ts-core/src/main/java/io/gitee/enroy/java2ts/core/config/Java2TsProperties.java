package io.gitee.enroy.java2ts.core.config;

import lombok.Data;


/**
 * chaos
 */
@Data
public class Java2TsProperties {
    public static final String PREFIX = "java2ts";
    public static final String ENABLE = PREFIX + ".enabled";
    /**
     * 是否开启,用于Java2tsAutoConfiguration
     */
    private boolean enabled = true;
    /**
     * api扫描路径
     */
    private String baseScanPackage;
    /**
     * 文件输出根目录
     */
    private String outputPath = "java2ts-ui";
    /**
     * 相对outputPath的api根目录
     */
    private String apiRootPath = "api";
    /**
     * 相对outputPath的model根目录
     */
    private String modelRootPath = "model";
    /**
     * 生成的api和model的文件名后缀
     */
    private String fileExtension = "ts";
    /**
     * src目录替代符
     */
    private String srcPath = "@";
    /**
     * import及变量定义是否以";"结尾
     */
    private boolean semicolonEnd = true;
    /**
     * ApiClient文件名
     */
    private String apiClientFactoryName = "ApiClientFactory";
    /**
     * ApiClient的client名
     */
    private String apiClientInstanceMethod = "client()";
    /**
     * promise.then
     */
    private String promiseResolveReturn = "data";
    /**
     * 可为空对象声明，通常在.d.ts文件中增加：declare type Nullable<T> = T | null | undefined; // 空
     */
    private String nullableDeclare = "Nullable";
}
