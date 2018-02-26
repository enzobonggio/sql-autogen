package com.rfsc.autogen.service;

import com.google.common.base.CaseFormat;
import com.rfsc.autogen.model.sql.Query;
import com.rfsc.autogen.model.sql.QueryParam;
import io.vavr.Tuple;
import io.vavr.Value;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.function.Function;

@Slf4j
@Component
public class QueryHandler {

    private final HashMap<Query.Type, Function<Query, ResultSetMetaData>> typeStrategy;
    private final ResultSetExtractor<ResultSetMetaData> extractor = (ResultSetExtractor<ResultSetMetaData>) ResultSet::getMetaData;

    public QueryHandler(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        typeStrategy = HashMap.of(
                Query.Type.NULL, query -> jdbcTemplate.query(query.getSql(), extractor),
                Query.Type.ARRAY, query -> jdbcTemplate.query(query.getSql(), query.getArrayParams(), extractor),
                Query.Type.MAP, query -> namedParameterJdbcTemplate.query(query.getSql(), query.getMapParams(), extractor),
                Query.Type.OBJECT, query -> namedParameterJdbcTemplate.query(query.getSql(), new BeanPropertySqlParameterSource(query.getObjectParam()), extractor));
    }

    public List<QueryParam> handle(Query query) {
        return typeStrategy.get(query.getType()).toTry()
                .map(f -> f.apply(query))
                .flatMap(resultSetMetaData -> Try.of(resultSetMetaData::getColumnCount)
                        .map(size -> Tuple.of(resultSetMetaData, size).map2(i -> List.range(1, i))))
                .flatMap(t -> Try.sequence(t._2().map(index -> tryQueryParam(index, t._1()))))
                .recoverWith(handleException())
                .toEither()
                .map(Value::toList)
                .getOrElse(List.empty());
    }

    private Try<QueryParam> tryQueryParam(Integer index, ResultSetMetaData metaData) {
        return Try.of(() -> {
            final String name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, metaData.getColumnName(index));
            final String className = metaData.getColumnClassName(index);
            final String typeName = metaData.getColumnTypeName(index);
            final int scale = metaData.getScale(index);
            final int precision = metaData.getPrecision(index);

            return QueryParam.of(name, className, typeName, scale, precision);
        });
    }

    private Function<Throwable, Try<? extends Seq<QueryParam>>> handleException() {
        return ex -> {
            log.error("Error when trying to create List of Params: {}", ex.getMessage());
            return Try.failure(new RuntimeException(ex));
        };
    }
}
