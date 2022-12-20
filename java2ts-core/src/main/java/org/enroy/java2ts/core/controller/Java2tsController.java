package org.enroy.java2ts.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.enroy.java2ts.core.commons.FileUtil;
import org.enroy.java2ts.core.rt.resolver.loader.ApiScanResolver;
import org.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RestController
public class Java2tsController {
    @Autowired
    private RuntimeProcessor processor;
    @Autowired
    private ApiScanResolver apiScanResolver;

    @GetMapping(value = "ts/download")
    public void downloadTs(HttpServletResponse response) throws Exception {
        // 扫描符合条件的class
        List<Class<?>> apiClasses = apiScanResolver.scanApis();
        if (CollectionUtils.isEmpty(apiClasses)) {
            log.warn("没有符合条件的api");
            return;
        }
        //处理扫描出的api
        File root = processor.process(apiClasses);
        // 下载文件
        doDownLoad(root, response);
    }

    /**
     * 文件打包下载
     */
    private void doDownLoad(File root, HttpServletResponse response) throws IOException {
        //生成压缩包
        FileUtil.fileToZip(root.getAbsolutePath(), root.getAbsolutePath(), "download.zip");
        //写入响应体
        String zipFilePath = root.getAbsolutePath() + "/download.zip";
        File file = new File(zipFilePath);
        String filename = file.getName();
        InputStream fis = new BufferedInputStream(Files.newInputStream(file.toPath()));
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        response.reset();
        // 设置response的Header
        response.addHeader("Content-Length", "" + file.length());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-disposition", "attachment;filename=" + new String(filename.getBytes()));

        OutputStream os = new BufferedOutputStream(response.getOutputStream());
        os.write(buffer);
        os.flush();
        os.close();
    }
}
