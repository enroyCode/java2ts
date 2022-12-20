package com.enroy.java2ts.core.rt.resolver.file;

import com.enroy.java2ts.core.commons.FileUtil;
import com.enroy.java2ts.core.rt.RuntimeProcessor;

import java.io.File;

/**
 * @author chaos
 */
public class WorkspaceResolver {
    private final RuntimeProcessor config;

    public WorkspaceResolver(RuntimeProcessor config) {
        this.config = config;
    }

    /**
     * 初始化输出文件夹
     */
    public File initOutPutRoot() {
        String outputPath = config.getOutputPath();
        if (outputPath.contains(".")) {
            throw new IllegalArgumentException("输出目录必须为文件夹");
        }
        File root = new File(outputPath);
        if (!root.exists()) {
            root.mkdirs();
        } else {
            FileUtil.clearDir(new File(root, ""));
        }
        return root;
    }
}
