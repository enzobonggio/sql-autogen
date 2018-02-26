package com.rfsc.autogen.model.template;

import lombok.Data;

@Data
public class Method {
    String methodName;
    Params params;
    ValueObject valueObject;
    Boolean singleValue;

    @Override
    public String toString() {
        return methodName;
    }
}
