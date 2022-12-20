package com.enroy.java2ts.core.rt.resolver.file;

import com.enroy.java2ts.core.commons.Consts;
import com.enroy.java2ts.core.config.IfThenClassCondition;
import com.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 目录处理器
 *
 * @author chaos
 */
public class OutputPathResolver {
    private final RuntimeProcessor config;

    public OutputPathResolver(RuntimeProcessor config) {
        this.config = config;
    }

    /**
     * 获取文件系统中相对于输出目录文件的目录，不包含文件名
     *
     * @param type     指定类型
     * @param rootPath 实体(api or model)输出的根目录
     */
    public String getJavaFilePath(Type type, String rootPath) {
        if (type instanceof Class) {
            return getJavaFilePath((Class<?>) type, rootPath);
        }
        throw new IllegalArgumentException("不支持的类型:" + type);
    }

    /**
     * 获取ts文件引用路径，File.separator转换为 "/"
     *
     * @param type     指定类型
     * @param rootPath 实体(api or model)输出的根目录
     */
    public String getTsImportPath(Type type, String rootPath) {
        if (type instanceof Class) {
            return getTsImportPath((Class<?>) type, rootPath);
        }
        throw new IllegalArgumentException("不支持的类型:" + type);
    }

    /**
     * ts文件引用路径，需要将File.separator替换为 "/"
     */
    private String getTsImportPath(Class<?> cls, String rootPath) {
        String relativePath = getJavaFilePath(cls, rootPath);
        return relativePath.replace(File.separator, Consts.SLASH);
    }

    /**
     * 获取class对应的输出目录地址 ，包路径转成文件路径
     */
    private String getJavaFilePath(Class<?> cls, String rootPath) {
        List<IfThenClassCondition<String>> filePathRules = config.getFilePathRules();
        String path = "";
        if (!CollectionUtils.isEmpty(filePathRules)) {
            for (IfThenClassCondition<String> rule : filePathRules) {
                if (rule.getIfCond().test(cls)) {
                    path = rule.getThen().apply(cls);
                    break;
                }
            }
        }
        path = rootPath + Consts.PERIOD + path;
        path = path.replace("..", Consts.PERIOD);
        return path.replace(Consts.PERIOD, File.separator);
    }

}
