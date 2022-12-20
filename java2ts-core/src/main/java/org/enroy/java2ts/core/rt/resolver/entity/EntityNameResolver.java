package org.enroy.java2ts.core.rt.resolver.entity;

import org.enroy.java2ts.core.config.IfThenClassCondition;
import org.enroy.java2ts.core.rt.RuntimeProcessor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 实体名处理器
 *
 * @author chaos
 */
public class EntityNameResolver {
    private final List<IfThenClassCondition<String>> entityRenameRules;

    public EntityNameResolver(RuntimeProcessor config) {
        this.entityRenameRules = config.getEntityRenameRules();
    }

    /**
     * 获取实体名称，用于文件名
     *
     * @param clazz 实体
     */
    public String buildEntityName(Class<?> clazz) {
        if (!CollectionUtils.isEmpty(entityRenameRules)) {
            for (IfThenClassCondition<String> rule : entityRenameRules) {
                if (rule.getIfCond().test(clazz)) {
                    return rule.getThen().apply(clazz);
                }
            }
        }
        return clazz.getSimpleName();
    }
}
