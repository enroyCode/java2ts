package com.enroy.java2ts.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 提供扩展功能
 *
 * @author chaos
 */
@Data
public class RuntimeExProperties {
    /**
     * 筛除指定api
     */
    private List<Predicate<Class<?>>> apiFilters;
    /**
     * 设置实体存放的路径规则
     */
    private List<IfThenClassCondition<String>> filePathRules;
    /**
     * 实体改名规则
     */
    private List<IfThenClassCondition<String>> entityRenameRules;


    public static RuntimeExConfigBuilder builder() {
        return new RuntimeExConfigBuilder();
    }

    public static class RuntimeExConfigBuilder {
        private final List<Predicate<Class<?>>> apiFilters = new ArrayList<>();
        private final List<IfThenClassCondition<String>> filePathRules = new ArrayList<>();
        private final List<IfThenClassCondition<String>> entityRenameRules = new ArrayList<>();

        /**
         * 忽略指定class的api
         *
         * @param ignoreClass api类
         */
        public RuntimeExConfigBuilder apiFilter(Class<?> ignoreClass) {
            apiFilters.add((cls) -> ignoreClass.getName().equals(cls.getName()));
            return this;
        }

        /**
         * 增加忽略的api策略过滤器
         *
         * @param filter 策略过滤器
         */
        public RuntimeExConfigBuilder apiFilter(Predicate<Class<?>> filter) {
            apiFilters.add(filter);
            return this;
        }

        /**
         * 调整api or model的文件引用路径
         *
         * @param ifCond 检测条件
         * @param then   执行条件
         */
        public RuntimeExConfigBuilder modelPathRule(Predicate<Class<?>> ifCond, Function<Class<?>, String> then) {
            filePathRules.add(new IfThenClassCondition<>(ifCond, then));
            return this;
        }

        /**
         * 调整model模型的文件引用路径
         *
         * @param filePathRule 设置modelPath的规则
         */
        public RuntimeExConfigBuilder modelPathRule(IfThenClassCondition<String> filePathRule) {
            filePathRules.add(filePathRule);
            return this;
        }

        public RuntimeExConfigBuilder entityRenameRule(Predicate<Class<?>> ifCond, Function<Class<?>, String> then) {
            entityRenameRules.add(new IfThenClassCondition<>(ifCond, then));
            return this;
        }

        /**
         * 修改实体后缀
         */
        public RuntimeExConfigBuilder entityRenameSuffix(String source, String target) {
            entityRenameRule((cls) -> cls.getSimpleName().endsWith(source),//
                    (cls) -> {
                        String name = cls.getSimpleName();
                        int idx = name.lastIndexOf(source);
                        return name.substring(0, idx) + target;
                    });
            return this;
        }

        /**
         * 修改实体
         */
        public RuntimeExConfigBuilder entityRename(Class<?> sourceCls, String target) {
            entityRenameRule((cls) -> sourceCls.getName().equals(cls.getName()),//
                    (cls) -> target);
            return this;
        }


        /**
         * 实体重命名
         *
         * @param entityRenameRule 重命名规则
         */
        public RuntimeExConfigBuilder entityRenameRule(IfThenClassCondition<String> entityRenameRule) {
            this.entityRenameRules.add(entityRenameRule);
            return this;
        }

        public RuntimeExProperties build() {
            RuntimeExProperties config = new RuntimeExProperties();
            config.apiFilters = this.apiFilters;
            config.filePathRules = this.filePathRules;
            config.entityRenameRules = this.entityRenameRules;
            return config;
        }
    }
}
