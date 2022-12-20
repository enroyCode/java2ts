package org.enroy.java2ts.core.rt;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

@Getter
@Setter
public class ClassMethod {
    private String name; // 方法名
    private String[] parameters = new String[0]; // 类型的全类名。不要设为null，懒得判空指针

    public ClassMethod(String name, String[] parameters) {
        this.name = name;
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public void setParameters(String[] parameters) {
        if (parameters == null) {
            this.parameters = new String[0];
        } else {
            this.parameters = parameters;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassMethod that = (ClassMethod) o;
        if (parameters.length != that.parameters.length) {
            return false;
        }
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (!Objects.equals(parameters[i], that.parameters[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ArrayUtils.addAll(parameters, name));
    }
}