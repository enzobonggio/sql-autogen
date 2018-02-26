package com.rfsc.autogen.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.rfsc.autogen.model.Config;
import com.rfsc.autogen.model.sql.Query;
import io.vavr.collection.HashMap;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ParamsDeserializer extends JsonDeserializer<Config.Query.Params> {
    private final Map<Query.Type, Function<JsonNode, List<Config.Query.Params.Param>>> paramsDeserializerStrategy = HashMap.of(
            Query.Type.ARRAY, this::processArrayQueryType
    );

    private final Map<Class<?>, Object> defaultValueMap = HashMap.of(
            Integer.class, 1,
            String.class, "String"
    );

    private final Map<Class<?>, Function<String, Object>> defaultValueFormatter = HashMap.of(
            Integer.class, (Integer::parseInt),
            String.class, s -> s
    );

    @Override
    public Config.Query.Params deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final Query.Type queryType = Query.Type.valueOf(node.get("type").asText());

        return Config.Query.Params.of(queryType, Option.of(queryType)
                .flatMap(paramsDeserializerStrategy::get)
                .map(function -> function.apply(node))
                .getOrElse((List<Config.Query.Params.Param>) Collections.EMPTY_LIST));
    }

    private List<Config.Query.Params.Param> processArrayQueryType(JsonNode jsonNode) {
        return Iterator.ofAll(jsonNode.get("content").iterator())
                .map(paramNode -> {
                    final String className = Config.ParamType.valueOf(paramNode.get("type").asText())
                            .getClassName();
                    final String paramName = paramNode.get("name").asText();
                    final Class<?> type = Try.of(() -> Class.forName(className)).getOrNull();
                    final Option<Object> defaultValue = paramNode.has("value") ?
                            defaultValueFormatter.get(type).map(f -> f.apply(paramNode.get("value").asText())) :
                            defaultValueMap.get(type);
                    return new Config.Query.Params.Param(className, paramName, defaultValue.getOrNull());

                })
                .toList()
                .toJavaList();
    }

}
