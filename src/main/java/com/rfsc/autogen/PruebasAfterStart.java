package com.rfsc.autogen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.rfsc.autogen.common.Utils;
import com.rfsc.autogen.model.Config;
import com.rfsc.autogen.model.sql.Query;
import com.rfsc.autogen.model.sql.QueryParam;
import com.rfsc.autogen.model.template.Class;
import com.rfsc.autogen.model.template.Method;
import com.rfsc.autogen.model.template.Param;
import com.rfsc.autogen.model.template.Params;
import com.rfsc.autogen.model.template.ValueObject;
import com.rfsc.autogen.service.QueryHandler;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class PruebasAfterStart {

    @Autowired
    public QueryHandler queryHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = mapper.readValue(new ClassPathResource("autogen.yml").getFile(), Config.class);
        List.ofAll(config.getQueries())
                .map(configQuery -> Tuple.of(config, configQuery, Query.builder()
                        .sql(Utils.readPath(configQuery.getPath()))
                        .type(configQuery.getParams() != null ?
                                configQuery.getParams().getType() :
                                Query.Type.NULL)
                        .arrayParams(configQuery.getParams() != null ?
                                List.ofAll(configQuery.getParams().getContent()).map(Config.Query.Params.Param::getDefaultValue).toJavaArray() :
                                null)
                        .build()))
                .map(t -> t.map3(q -> queryHandler.handle(q)))
                .map(t -> {
                    Class clazz = new Class();
                    Set<String> extraPackages = new HashSet<>();

                    extraPackages.addAll(t._3().map(QueryParam::getClassName).toJavaList());

                    extraPackages.addAll(Option.of(t._2().getParams())
                            .map(Config.Query.Params::getContent)
                            .map(List::ofAll)
                            .map(params -> params.map(Config.Query.Params.Param::getClassName))
                            .getOrElse(List.empty())
                            .toJavaList());

                    ValueObject valueObject = new ValueObject();
                    valueObject.setValueObjectName(t._2().getValueObjectName());

                    final Params paramsOut = Params.instance();
                    paramsOut.addAll(t._3().map(q -> Param.of(q.getName(), q.getSimpleClassName())).toJavaList());
                    valueObject.setParams(paramsOut);

                    Method pruebaMethod = new Method();
                    pruebaMethod.setMethodName(t._2().getMethodName());

                    final Params paramsIn = Params.instance();
                    paramsIn.addAll(Option.of(t._2().getParams())
                            .map(Config.Query.Params::getContent)
                            .map(List::ofAll)
                            .map(params -> params.map(Param::new))
                            .getOrElse(List.empty())
                            .toJavaList());
                    pruebaMethod.setParams(paramsIn);
                    pruebaMethod.setValueObject(valueObject);
                    pruebaMethod.setSingleValue(false);

                    clazz.setClassName(t._2.getDaoName());
                    clazz.setPackageName(config.getRootPackage());
                    clazz.setMethods(Collections.singleton(pruebaMethod));
                    clazz.setValueObjects(Collections.singleton(valueObject));
                    clazz.setExtraImports(extraPackages);

                    return clazz;
                })
                .forEach(c -> {
                    MustacheFactory mf = new DefaultMustacheFactory();
                    Mustache mustache = mf.compile("templates/Query.mustache");
                    Try.of(() -> {
                        mustache.execute(new PrintWriter(System.out), c).flush();
                        return null;
                    }).get();
                });
    }
}
