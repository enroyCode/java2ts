package io.gitee.enroy.java2ts.core.rt;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * 通过池化技术减少
 *
 * @author zhuchao
 */
public class TypeProcessPool {
    /**
     * 处理过的
     */
    private final Set<Type> processed = new HashSet<>();
    /**
     * 收集的
     */
    private final Set<Type> unprocessed = new HashSet<>();

    public void add(Type cls) {
        if (this.processed.contains(cls)) {
            return;
        }
        this.unprocessed.add(cls);
    }

    public void add(TypeProcessPool pool) {
        this.unprocessed.addAll(pool.unprocessed);
    }

    public Type next() {
        if (CollectionUtils.isEmpty(this.unprocessed)) {
            return null;
        }
        Type node = this.unprocessed.iterator().next();
        this.unprocessed.remove(node);
        this.processed.add(node);
        return node;
    }
}
