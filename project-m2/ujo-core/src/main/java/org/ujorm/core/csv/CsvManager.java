package org.ujorm.core.csv;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.tools.common.Primitive;
import org.ujorm.tools.common.StreamUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvManager<D> {

    private final DomainHandler<D> domainHandler;
    private final CsvConfig config;
    private final Map<String, Key<D,?>> keyMap;
    private final int maxFields;
    private final CsvLineSplitter splitter;
    private final KeyFun<D, ?>[] keyFuns;

    public CsvManager(DomainHandler<D> domainHandler, CsvConfig csvConfig) {
        this.domainHandler = domainHandler;
        this.config = csvConfig;
        this.keyMap = StreamUtils.map(Key::name, domainHandler.getKeyList());
        this.maxFields = domainHandler.count();
        this.keyFuns = new KeyFun[domainHandler.count()];
        this.splitter = csvConfig.splitter();
        init();
    }

    /** Init key functions */
    private void init() {
        for (var key : domainHandler.getKeyList()) {
            var type = Primitive.wrapPrimitive(key.type());
            var fun = this.config.converterMap().get(type);

            if (fun == null) {
                var msg = "Property %s type of %s has no converter".formatted(key.fullName(), key.type().getSimpleName());
                throw new IllegalArgumentException(msg);
            }
            keyFuns[key.index()] = new KeyFun(key, fun);
        }
    }

    public Stream<D> convertByOrder(Stream<String> lines) {
        return lines.map( line -> {
            var result = AbstractUjo.of(domainHandler);
            var texts = splitter.split(line, maxFields);
            var max = Math.min(texts.length, keyFuns.length);
            for (int i = 0; i < max; i++) {
                var text = texts[i];
                var keyFun = keyFuns[i];
                var key = (Key<D, Object>) keyFun.key;
                var value = (!text.isEmpty() || key.type().equals(String.class))
                        ? keyFun.fun.apply(text)
                        : null;
                result.setValue(key, value);
            }
            return result.buildDomain();
        });
    }

    @Deprecated
    public Stream<D> convertByName(Stream<String> line) {
        throw new IllegalArgumentException("TODO");
    }

    record KeyFun<D, V> (Key<D,V> key, Function<String, V> fun) {}

    public static <D> CsvManager<D> of(Class<D> domainClass) {
        return of(domainClass, CsvConfig.ofDefault());
    }

    public static <D> CsvManager<D> of(Class<D> domainClass, CsvConfig csvConfig) {
        return new CsvManager<>(DomainHandlerProvider.getHandler(domainClass),csvConfig);
    }
}
