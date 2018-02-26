package com.rfsc.autogen.model.template;

import lombok.Data;

import java.util.Set;

@Data
public class Class {
    String className;
    String packageName;
    Set<Method> methods;
    Set<ValueObject> valueObjects;
    Set<String> extraImports;

    @Override
    public String toString() {
        return className;
    }
}
