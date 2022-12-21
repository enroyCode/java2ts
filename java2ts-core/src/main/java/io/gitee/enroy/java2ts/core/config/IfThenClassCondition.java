package io.gitee.enroy.java2ts.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author chaos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IfThenClassCondition<R> {
    private Predicate<Class<?>> ifCond;
    private Function<Class<?>, R> then;
}
