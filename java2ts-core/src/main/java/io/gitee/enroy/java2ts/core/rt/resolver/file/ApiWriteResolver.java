package io.gitee.enroy.java2ts.core.rt.resolver.file;

import io.gitee.enroy.java2ts.core.commons.Consts;
import io.gitee.enroy.java2ts.core.entity.ApiEntity;
import io.gitee.enroy.java2ts.core.entity.ApiMethodEntity;
import io.gitee.enroy.java2ts.core.entity.TypeParameter;
import io.gitee.enroy.java2ts.core.rt.RuntimeProcessor;
import io.gitee.enroy.java2ts.core.rt.TypeProcessPool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chaos
 */
public class ApiWriteResolver extends AbstractWriteResolver {

    public ApiWriteResolver(RuntimeProcessor config) {
        super(config);
    }

    /**
     * api文件写入
     */
    public TypeProcessPool writeApisAndFindModels(List<ApiEntity> apis, File root) throws Exception {
        TypeProcessPool collector = new TypeProcessPool();
        // 写api对象文件
        for (ApiEntity api : apis) {
            Writer apiWriter = null;
            try {
                //获取文件path
                String apiFilePath = config.getOutputPathResolver().getJavaFilePath(api.getType(), config.getApiRootPath());
                File dir = new File(root, apiFilePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                //新建api对象文件
                File file = new File(dir, api.getName() + "." + config.getFileExtension());
                if (!file.exists()) {
                    file.createNewFile();
                }
                apiWriter = new FileWriter(file);
                writeApi(api, apiWriter, collector);
            } finally {
                if (apiWriter != null) {
                    apiWriter.close();
                }
            }
        }
        return collector;
    }

    /* ${apiFile} */

//  ${imports}
//
//  /**
//   * ${apiNode}
//   */
//  export default class ${ApiName} {
//
//      ${methods}
//
//  }

    /* ${methods} */

//      /**
//       * ${nodes}
//       */
//      static ${methodName}(${params}): Promise<${rtnType}> {
//          ${client}
//      }

    /* ${client} */

//          return ${ApiClientFactory}.${ClientInstanceMethod}).${RequestMethod()}(${url}, ${data}?, {
//              ${config}
//          }).then((res) => {
//              return res.data
//          })

    /**
     * step1: 写入头部，import ApiClientFactory区
     * step2:
     */
    public void writeApi(ApiEntity api, Writer apiWriter, TypeProcessPool typeProcessPool) throws IOException {
        StringBuilder fileSb = new StringBuilder("${imports}\n" +
                "${apiNodes}" +
                "export default class ${apiName} {\n" +
                "${methods}" +
                "\n}"
        );
        StringBuilder importsSb = new StringBuilder();
        StringBuilder apiNoteSb = new StringBuilder();
        StringBuilder methodsSb = new StringBuilder();
        // 写入方法
        // 按方法名排序
        Collections.sort(api.getMethods());
        String apiPath = api.getPath();
        TypeProcessPool methodTypePool = new TypeProcessPool();// 收集该api实体中所有的模型对象
        //遍历方法并写入
        for (ApiMethodEntity method : api.getMethods()) {
            // 预处理处理method注释
            preProcessMethodNote(method, methodsSb);
            // 写入方法
            Map<String, String> paramNotes = processApiMethod(method, apiPath, methodsSb, methodTypePool);
            //处理method参数注释
            processMethodParamNotes(method, paramNotes, methodsSb);
        }
        //
        typeProcessPool.add(methodTypePool);
        // 处理import区
        processImports(api.getType(), methodTypePool, importsSb, processApiClientFactoryNameImport());
        // 处理api注释区
        processApiNote(api, apiNoteSb);
        // 构造文件
        replace(fileSb, "${imports}", importsSb);
        replace(fileSb, "${apiNodes}", apiNoteSb);
        replace(fileSb, "${apiName}", api.getName());
        replace(fileSb, "${methods}", methodsSb);
        // 写入文件
        write(apiWriter, fileSb);
    }

    private void processApiNote(ApiEntity api, StringBuilder apiNoteSb) {
        if (StringUtils.isNotBlank(api.getNote())) {
            apiNoteSb.append(notes(0, true, api.getNote()));
            apiNoteSb.append(Consts.ENTER);
        }
    }

    private String processApiClientFactoryNameImport() throws IOException {
        // import ApiClientFactoryName from 'SrcPath/ApiRootPath/ApiClientFactoryName';
        String importStr = String.format("import %s from '%s/%s/%s'", //
                config.getApiClientFactoryName(), //
                config.getSrcPath(), //
                config.getApiRootPath(),//
                config.getApiClientFactoryName());//
        if (config.isSemicolonEnd()) {
            importStr += Consts.SEMICOLON;
        }
        importStr += Consts.ENTER;
        return importStr;
    }

    private void processMethodParamNotes(ApiMethodEntity method, Map<String, String> paramNotes, StringBuilder methodsSb) {
        if (CollectionUtils.isEmpty(paramNotes)) {
            int index = methodsSb.lastIndexOf("${paramNote}");
            methodsSb.replace(index, "${paramNote}".length() + index, "");
        } else {
            StringBuilder notesSb = new StringBuilder();
            for (Map.Entry<String, String> noteEntry : paramNotes.entrySet()) {
                notesSb.append(Consts.ENTER).append(Consts.TAB).append(" * ").append(String.format("@param %s %s", noteEntry.getKey(), noteEntry.getValue()));
            }
            replace(methodsSb, "${paramNote}", notesSb);
        }
    }

    private Map<String, String> processApiMethod(ApiMethodEntity method, String apiPath, StringBuilder methodsSb, TypeProcessPool importsClassPool) {
        Map<String, String> paramsNote = new LinkedHashMap<>();
        StringBuilder methodSb = new StringBuilder("\n\tstatic ${methodName}(${methodParam}): Promise<${returnType}> {\n"//
                + "${client}\n" //
                + "\t}\n");//
        // 方法名
        replace(methodSb, "${methodName}", method.getName());

        // builder
        StringBuilder methodParamSb = new StringBuilder();
        StringBuilder returnTypeSb = new StringBuilder();
        StringBuilder clientDetailSb = new StringBuilder("\t\treturn ${api}.${httpMethod}(${clientParams}).then((res) => {\n\t\t\treturn ${promiseResolveReturn}\n\t\t})");

        StringBuilder clientParamsSb = new StringBuilder();
        StringBuilder apiSb = new StringBuilder();
        apiSb.append(config.getApiClientFactoryName());
        if (StringUtils.isNotBlank(config.getApiClientInstanceMethod())) {
            apiSb.append(Consts.PERIOD).append(config.getApiClientInstanceMethod());
        }
        // 构造请求客户端
        replace(clientDetailSb, "${api}", apiSb);
        // 构造请求方法
        replace(clientDetailSb, "${httpMethod}", method.getRequestMethod().toLowerCase());
        // 请求路径
        clientParamsSb.append(String.format("`%s`,", (apiPath + "/" + method.getPath()).replace("//", "/")));
        boolean methodParamEndWithComma = false;// methodParamSb是否存在逗号结尾
        boolean clientParamEndWithComma = false;// clientParam是否存在逗号结尾
        boolean mayHasBody = method.mayHasBody();
        boolean hasConfigParams = method.hasConfigParams();
        List<TypeParameter> notRequired = new ArrayList<>();//对于ts来说，方法名不可以重复
        // 处理path参数
        if (!CollectionUtils.isEmpty(method.getPaths())) {
            methodParamEndWithComma = true;
            for (TypeParameter parameter : method.getPaths()) {
                if (parameter.isRequired()) {
                    encodeMethodParam(parameter, methodParamSb, paramsNote, importsClassPool);
                } else {
                    notRequired.add(parameter);
                }
            }
        }
        // 处理body参数
        if (mayHasBody) {
            if (method.getBody() != null) {
                methodParamEndWithComma = true;
                clientParamEndWithComma = true;
                TypeParameter parameter = method.getBody();
                parameter.setName("body");
                encodeMethodParam(parameter, methodParamSb, paramsNote, importsClassPool);
                clientParamsSb.append(Consts.SPACE).append(parameter.getName()).append(Consts.COMMA);
            } else {
                clientParamsSb.append("null");
            }
        }
        if (hasConfigParams) {
            clientParamsSb.append(" {");
            // 处理header参数
            if (!CollectionUtils.isEmpty(method.getHeaders())) {
                methodParamEndWithComma = true;
                clientParamEndWithComma = true;
                clientParamsSb.append("\n\t\t\theaders: {");
                for (TypeParameter parameter : method.getHeaders()) {
                    clientParamsSb.append(String.format("\n\t\t\t\t%s: %s,", parameter.getName(), parameter.getName()));
                    if (parameter.isRequired()) {
                        encodeMethodParam(parameter, methodParamSb, paramsNote, importsClassPool);
                    } else {
                        notRequired.add(parameter);
                    }
                    clientParamsSb.delete(clientParamsSb.length() - 1, clientParamsSb.length());//去除最后的逗号
                    clientParamsSb.append("\n\t\t\t},");
                }
            }
            // 处理query参数
            if (!CollectionUtils.isEmpty(method.getQueries())) {
                methodParamEndWithComma = true;
                clientParamEndWithComma = true;
                clientParamsSb.append("\n\t\t\tparams: {");
                for (TypeParameter parameter : method.getQueries()) {
                    clientParamsSb.append(String.format("\n\t\t\t\t%s: %s,", parameter.getName(), parameter.getName()));
                    if (parameter.isRequired()) {
                        encodeMethodParam(parameter, methodParamSb, paramsNote, importsClassPool);
                    } else {
                        notRequired.add(parameter);
                    }
                    clientParamsSb.delete(clientParamsSb.length() - 1, clientParamsSb.length());
                    clientParamsSb.append("\n\t\t\t},");
                }
            }
            if (clientParamEndWithComma) {
                clientParamsSb.delete(clientParamsSb.length() - 1, clientParamsSb.length());
            }
            clientParamsSb.append("\n\t\t}");
        } else {
            clientParamsSb.append(" {}");
        }
        //处理非必填参数
        for (TypeParameter parameter : notRequired) {
            encodeMethodParam(parameter, methodParamSb, paramsNote, importsClassPool);
        }
        if (methodParamEndWithComma) {//存在形参，那么需要去掉encodeParam的最后的", "
            methodParamSb.delete(methodParamSb.length() - 2, methodParamSb.length());
        }
        replace(methodSb, "${methodParam}", methodParamSb);
        // 返回值
        encodeReturnType(method.getRtnType(), returnTypeSb, importsClassPool);
        replace(methodSb, "${returnType}", returnTypeSb);

        // client
//        clientDetailSb.append("\n\t\t}).then((res) => {")//
//                .append("\n\t\t\treturn res");
        replace(clientDetailSb, "${promiseResolveReturn}", StringUtils.isNotBlank(config.getPromiseResolveReturn()) ? "res." + config.getPromiseResolveReturn() : "res");

//        clientDetailSb.append("\n\t\t})");

        replace(clientDetailSb, "${clientParams}", clientParamsSb);

        replace(methodSb, "${client}", clientDetailSb);
        // 写入
        methodsSb.append(methodSb);
        return paramsNote;
    }

    private void encodeReturnType(Type rtnType, StringBuilder returnSb, TypeProcessPool importsClassPool) {
        String tsType = config.getJavaTypeResolver().java2TsType(rtnType, importsClassPool);
        returnSb.append(tsType);
    }

    /**
     * 处理api.method上的参数
     */
    private void encodeMethodParam(TypeParameter parameter, StringBuilder paramSb, Map<String, String> paramNote, TypeProcessPool importsClassPool) {
        // 获取java类型与ts类型的对应关系
        String tsType = config.getJavaTypeResolver().java2TsType(parameter.getType(), importsClassPool);
        paramSb.append(String.format("%s: %s, ", parameter.getName() + (parameter.isRequired() ? "" : "?"), tsType));
        paramNote.put(parameter.getName(), StringUtils.isNotBlank(parameter.getNote()) ? parameter.getNote() : "");
    }

    private void preProcessMethodNote(ApiMethodEntity method, StringBuilder sb) {
        String note;
        String paramStart = "${paramNote}";
        if (method.isDeprecated()) {
            note = notes(1, false, method.getNote(), paramStart, "@deprecated 已废弃");
        } else {
            note = notes(1, false, method.getNote(), paramStart);
        }
        sb.append("\n").append(note);
    }
}
