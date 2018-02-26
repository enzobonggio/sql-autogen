package com.rfsc.autogen.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class Utils {

    public String readPath(String path) {
        return Try.of(() -> Files.lines(Paths.get(new ClassPathResource(path).getURI()))
                .collect(Collectors.joining(" ")))
                .onFailure(ex -> log.error("Error when read path {} ", path, ex))
                .getOrElse("");
    }

    public String classSimpleName(String className) {
        return Try.of(() -> Class.forName(className))
                .map(Class::getSimpleName)
                .getOrElse("");
    }
}
