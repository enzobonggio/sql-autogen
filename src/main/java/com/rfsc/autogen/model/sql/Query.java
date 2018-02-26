package com.rfsc.autogen.model.sql;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Query {
    private String sql;
    private Map<String, Object> mapParams;
    private Object objectParam;
    private Object[] arrayParams;
    private Type type;

    public enum Type {
        NULL,
        ARRAY,
        OBJECT,
        MAP
    }
}
