package org.enroy.java2ts.core.commons;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FileUtil {
    private static final int BUFFER_SIZE = 10 * 1024;

    /**
     * 清空目录
     */
    public static void clearDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            assert children != null;
            for (File file : children) {
                delete(file);
            }
        }
    }

    /**
     * 删除文件
     */
    public static void delete(File file) {
        boolean hasRetain = false;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            assert children != null;
            for (File child : children) {
                delete(child);
            }
            if (Objects.requireNonNull(file.listFiles()).length > 0) {
                hasRetain = true;
            }
        }
        if (hasRetain) {
            // 文件夹下存在保留文件
            return;
        }
        boolean b = file.delete();
        if (!b) {
            throw new IllegalArgumentException("删除文件失败：" + file);
        }
    }

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     *
     * @param sourceFilePath :待压缩的文件路径
     * @param zipFilePath    :压缩后存放路径
     * @param fileName       :压缩后文件的名称
     */
    public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) {
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if (!sourceFile.exists()) {
            log.info("待压缩的文件目录：{}不存在", sourceFilePath);
        } else {
            try {
                if (!fileName.endsWith(".zip")) {
                    fileName = fileName + ".zip";
                }
                File zipFile = new File(zipFilePath, fileName);
                File[] sourceFiles = sourceFile.listFiles();
                if (null == sourceFiles || sourceFiles.length < 1) {
                    log.info("待压缩的文件目录：<{}>里面不存在文件，无需压缩", sourceFilePath);
                } else {
                    fos = new FileOutputStream(zipFile);
                    zos = new ZipOutputStream(new BufferedOutputStream(fos));
                    for (File file : sourceFiles) {
                        compress(file, zos, file.getName(), true);
                    }
                    flag = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                // 关闭流
                try {
                    if (null != zos)
                        zos.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        return flag;
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure)
            throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + File.separator));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + File.separator + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }
                }
            }
        }
    }
}
