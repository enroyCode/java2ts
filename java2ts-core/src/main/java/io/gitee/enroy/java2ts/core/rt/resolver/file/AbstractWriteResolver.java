package io.gitee.enroy.java2ts.core.rt.resolver.file;

import io.gitee.enroy.java2ts.core.commons.Consts;
import io.gitee.enroy.java2ts.core.rt.BasicsModelIgnore;
import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import io.gitee.enroy.java2ts.core.rt.TypeProcessPool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author chaos
 */
public class AbstractWriteResolver {
    protected final RuntimeProcessor config;

    public AbstractWriteResolver(RuntimeProcessor config) {
        this.config = config;
    }

    protected void write(Writer writer, StringBuilder sb) throws IOException {
        writer.write(sb.toString()/*.replace(TAB, "  ")*/);
    }

    protected void write(Writer writer, String content) throws IOException {
        writer.write(content/*.replace(TAB, "  ")*/);
    }

    /**
     * 根据换行分隔(不支持\r\n 和 \r)
     */
    protected List<String> splitByLineFeed(String str) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(str)) {
            return result;
        }
        return Arrays.asList(str.split(Consts.ENTER));
    }

    protected void processImports(Type thisType, TypeProcessPool importsClassPool, StringBuilder importsSb, String... appendImports) {
        if (thisType instanceof Class) {
            processImports(((Class<?>) thisType), importsClassPool, importsSb, appendImports);
        } else {
            throw new IllegalArgumentException("不支持的类型:" + thisType);
        }
    }

    private void processImports(Class<?> thisCls, TypeProcessPool importsClassPool, StringBuilder importsSb, String... appendImports) {
        Type type = importsClassPool.next();
        List<String> imports = new ArrayList<>();
        while (type != null) {
            if (BasicsModelIgnore.ignore(type)) {
                type = importsClassPool.next();
                continue;
            }
            boolean isEnum = false;
            if (type instanceof Class) {
                Class<?> cls = (Class<?>) type;
                isEnum = cls.isEnum();
                if (cls == thisCls) {
                    // 对于ts来讲，即使是同包下的也要import
                    continue;
                }
            }
            assert type instanceof Class<?>;
            String name = config.getEntityNameResolver().buildEntityName((Class<?>) type);//获取type的对应ts类型
            String clsPkg = config.getOutputPathResolver().getTsImportPath(type, config.getModelRootPath());
            String importStr;
            if (isEnum) {
                importStr = String.format("import { %s } from '%s/%s'", name, config.getSrcPath(), (clsPkg + "/" + name).replace("//", "/"));
            } else {
                importStr = String.format("import %s from '%s/%s'", name, config.getSrcPath(), (clsPkg + "/" + name).replace("//", "/"));
            }
            if (config.isSemicolonEnd()) {
                importStr += Consts.SEMICOLON_END_ENTER;
            } else {
                importStr += Consts.ENTER;
            }
            imports.add(importStr);
            type = importsClassPool.next();
        }
        if (appendImports != null) {
            Collections.addAll(imports, appendImports);
        }
        Collections.sort(imports);//对imports重新排序
        for (String s : imports) {
            importsSb.append(s);
        }
    }

    protected String notes(int tabSize, boolean forceMultiLineNotes, String... notes) {
        StringBuilder sb = new StringBuilder();
        if (notes == null || notes.length == 0) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String n : notes) {
            result.addAll(splitByLineFeed(n));
        }
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        StringBuilder tab = new StringBuilder();
        int i = 0;
        while (i++ < tabSize) {
            tab.append(Consts.TAB);
        }
        if (result.size() == 1 && !forceMultiLineNotes) {
            sb.append(tab).append("// ").append(result.get(0));
        } else {
            sb.append(tab).append("/**").append(Consts.ENTER);
            for (String n : result) {
                sb.append(tab).append(" * ").append(n).append(Consts.ENTER);
            }
            sb.append(tab).append(" */");
        }
        return sb.toString();
    }


    protected void replace(StringBuilder sb, String pattern, StringBuilder content) {
        replace(sb, pattern, content.toString());
    }

    protected void replace(StringBuilder sb, String pattern, String content) {
        int index = sb.lastIndexOf(pattern);
        sb.replace(index, index + pattern.length(), content);
    }
}
