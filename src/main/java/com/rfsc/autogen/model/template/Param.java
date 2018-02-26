package com.rfsc.autogen.model.template;

import com.rfsc.autogen.model.Config;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class Param {
    String paramName;
    String paramType;

    public Param(Config.Query.Params.Param p) {
        this.paramName = p.getName();
        this.paramType = p.getSimpleClassName();
    }

    @Override
    public String toString() {
        return paramType + " " + paramName;
    }
}
