package com.rfsc.autogen.model.sql;

import com.rfsc.autogen.common.Utils;
import lombok.Value;

@Value(staticConstructor = "of")
public class QueryParam {
    String name;
    String className;
    String typeName;
    int scale;
    int precision;

    public String getSimpleClassName() {
        return Utils.classSimpleName(className);
    }
}
