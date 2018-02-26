package com.rfsc.autogen.model.template;

import lombok.Data;

@Data
public class ValueObject {
    String valueObjectName;
    Params params;

    @Override
    public String toString() {
        return valueObjectName;
    }
}
