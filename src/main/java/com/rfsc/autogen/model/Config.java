package com.rfsc.autogen.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rfsc.autogen.common.Utils;
import com.rfsc.autogen.deserializer.ParamsDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Config {
    String rootPackage;
    List<Query> queries;

    public enum ParamType {
        INTEGER("java.lang.Integer"),
        STRING("java.lang.String");

        private String clazz;

        ParamType(String clazz) {
            this.clazz = clazz;
        }

        public String getClassName() {
            return clazz;
        }
    }

    @Data
    public static class Query {
        String path;
        String daoName;
        String methodName;
        String valueObjectName;
        @JsonDeserialize(using = ParamsDeserializer.class)
        Params params;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor(staticName = "of")
        public static class Params {
            com.rfsc.autogen.model.sql.Query.Type type;
            List<Param> content;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class Param {
                String className;
                String name;
                Object defaultValue;

                public String getSimpleClassName() {
                    return Utils.classSimpleName(className);
                }
            }
        }
    }
}
