package com.rfsc.autogen.model.template;

import java.util.HashSet;
import java.util.stream.Collectors;

public class Params extends HashSet<Param> {

    private Params() {
    }

    public static Params instance() {
        return new Params();
    }

    @Override
    public String toString() {
        return this.stream()
                .map(Param::toString)
                .collect(Collectors.joining(", "));
    }
}
