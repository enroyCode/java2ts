package com.enroy.java2ts.core.rt.resolver.file;

import com.enroy.java2ts.core.commons.ClassUtil;
import com.enroy.java2ts.core.entity.ModelEntity;
import com.enroy.java2ts.core.entity.TypeParameter;
import com.enroy.java2ts.core.rt.BasicsModelIgnore;
import com.enroy.java2ts.core.rt.RuntimeProcessor;
import com.enroy.java2ts.core.rt.TypeProcessPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * @author chaos
 */
public class ModelWriteResolver extends AbstractWriteResolver {
    public ModelWriteResolver(RuntimeProcessor config) {
        super(config);
    }

    public void writeModels(TypeProcessPool modelEntityProcessPool, File root) throws Exception {
        // 写model对象文件
        Type type = modelEntityProcessPool.next();
        Writer writer = null;
        while (null != type) {
            try {
                if (BasicsModelIgnore.ignore(type)) {
                    continue;
                }
                ModelEntity model = config.getModelEntityLoadResolver().buildModelEntity(type);
                if (model == null) {
                    continue;
                }
                String modelName = config.getEntityNameResolver().buildEntityName((Class<?>) type);
                model.setName(modelName);
                String path = config.getOutputPathResolver().getJavaFilePath(model.getType(), config.getModelRootPath());
                File dir = new File(root, path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                //新建model对象文件
                File file = new File(dir, model.getName() + "." + config.getFileExtension());
                if (!file.exists()) {
                    file.createNewFile();
                }
                writer = new FileWriter(file);
                write(model, writer, modelEntityProcessPool);
            } finally {
                if (writer != null) {
                    writer.close();
                }
                type = modelEntityProcessPool.next();
            }
        }
    }

    private void write(ModelEntity model, Writer writer, TypeProcessPool collector)
            throws IOException {
        StringBuilder modelSb = new StringBuilder("${imports}\n" + //
                "${modelNote}" + //
                "${modelClass}" +//
                "\n}");
        StringBuilder importsSb = new StringBuilder();
        StringBuilder modelNoteSb = new StringBuilder();
        StringBuilder modelClassSb = new StringBuilder();

        TypeProcessPool importsClassPool = new TypeProcessPool();
        // 处理note
        processModelNote(model, modelNoteSb);
        // 处理modelClass
        processModelClass(model, modelClassSb, importsClassPool);
        //文件结尾
        collector.add(importsClassPool);
        // 处理imports
        processImports(model.getType(), importsClassPool, importsSb);


        replace(modelSb, "${imports}", importsSb);
        replace(modelSb, "${modelNote}", modelNoteSb);
        replace(modelSb, "${modelClass}", modelClassSb);

        write(writer, modelSb);
    }

    private void processModelClass(ModelEntity model, StringBuilder modelClassSb,
                                   TypeProcessPool importsClassPool) {
        if (model.isEnum()) {
            processEnumModelClass(model, modelClassSb);
        } else {
            processNotEnumModelClass(model, modelClassSb, importsClassPool);
        }
    }

    private void processEnumModelClass(ModelEntity model, StringBuilder modelClassSb) {
        modelClassSb.append(String.format("export enum %s {", model.getName()));
        boolean hasComma = false;
        for (TypeParameter parameter : model.getEnums()) {
            String note = parameter.getNote();
            if (parameter.isDeprecated()) {
                note = notes(1, false, note, "@deprecated 已废弃");
            } else {
                note = notes(1, false, note);
            }
            if (note != null) {
                modelClassSb.append("\n").append(note);
            } else {
                modelClassSb.append("\n\t// ");
            }
            modelClassSb.append(String.format("\n\t%s = '%s',", parameter.getName(), parameter.getName()));
            hasComma = true;
        }
        if (hasComma) {//去除最后的逗号
            modelClassSb.delete(modelClassSb.length() - 1, modelClassSb.length());
        }
    }

    private void processNotEnumModelClass(ModelEntity model, StringBuilder modelClassSb,
                                          TypeProcessPool importsClassPool) {
        Type type = model.getType();
        String classTypeName = config.getModelTypeNameResolver().resolveModelTypeName(type, importsClassPool);
        if (model.getSuperType() != null // 有父类
                && !BasicsModelIgnore.ignore(type)) {// 父类不是基础类型
            String superClassTypeName = config.getJavaTypeResolver().java2TsType(model.getSuperType(), importsClassPool);
            // 父类
            modelClassSb.append(String.format("export default class %s extends %s {", classTypeName, superClassTypeName));
        } else {
            modelClassSb.append(String.format("export default class %s {", classTypeName));
        }
        for (TypeParameter parameter : model.getFields()) {
            //处理注释
            String note = parameter.getNote();
            if (parameter.isDeprecated()) {
                note = notes(1, false, note, "@deprecated 已废弃");
            } else {
                note = notes(1, false, note);
            }
            if (note != null) {
                modelClassSb.append("\n").append(note);
            } else {
                modelClassSb.append("\n\t// ");
            }
            modelClassSb.append("\n");
            String tsType = config.getJavaTypeResolver().java2TsType(parameter.getType(), importsClassPool);
            if ("any".equals(tsType)) {//未知，map等
                modelClassSb.append(String.format("\t%s: any", parameter.getName()));
            } else if (parameter.isRequired()) {//必填
                if (tsType.contains("[]")) {//数组、list集合
                    modelClassSb.append(String.format("\t%s: %s = %s", parameter.getName(), tsType, "[]"));
                } else {
                    Class<?> cls = ClassUtil.getTypeClass(type);
                    if (cls == null) {
                        modelClassSb.append(String.format("\t%s: %s<%s>", parameter.getName(), config.getNullableDeclare(), tsType));
                    } else {
                        String defaultValue = config.getDefaultValueResolver().defaultValue(cls, parameter.getDft());
                        if (defaultValue != null) {
                            modelClassSb.append(String.format("\t%s: %s = %s", parameter.getName(), tsType, defaultValue));
                        } else {
                            modelClassSb.append(String.format("\t%s: %s<%s>", parameter.getName(), config.getNullableDeclare(), tsType));
                        }
                    }
                }
            } else {//非必填
                if (tsType.contains("[]")) {
                    modelClassSb.append(String.format("\t%s: %s", parameter.getName(), tsType));
                } else {
                    modelClassSb.append(String.format("\t%s: %s<%s>", parameter.getName(), config.getNullableDeclare(), tsType));
                }
            }
            if (config.isSemicolonEnd()) {
                modelClassSb.append(";");
            }
        }
    }

    private void processModelNote(ModelEntity model, StringBuilder modelNoteSb) {
        String entityNote;
        if (model.isDeprecated()) {
            entityNote = notes(0, false, model.getNote(), "@deprecated 已废弃");
        } else {
            entityNote = notes(0, false, model.getNote());
        }
        if (entityNote != null) {
            modelNoteSb.append(entityNote).append("\n");
        }
    }
}
