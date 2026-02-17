package org.ujorm.mapper;

import org.ujorm.tools.msg.MessageService;

import java.io.StringWriter;
import java.util.HashMap;

public class JavaSourceGenerator {

    private final String PACKAGE_PREFIX = "org.ujorm.gen.";

    public String getSourceClass() {
        return "";
    }

    private String body(DomainModel meta) {
        var writer = new StringWriter(256);
        var params = new HashMap<String, Object>();
        {
            params.put("package", PACKAGE_PREFIX + meta.beanClass().getPackageName());
            params.put("generatedClass", meta.beanClass().getSimpleName() + "_");
            params.put("domainClass", meta.beanClass().getName());
            params.put("keys", "");
        }
        var templateBeg1 = """
            package ${package};
            import org.jetbrains.annotations.NotNull;
            import org.jetbrains.annotations.Nullable;
            import org.ujorm.core.Key;
            import org.ujorm.core.impl.AbstractKey;
            import org.ujorm.core.impl.AbstractMetaModel;
            
            public class ${generatedClass} extends AbstractMetaModel<${domainClass}> {
                private static final Class<${domainClass}> domainType = ${domainClass}.class;
                public ${generatedClass}() {
                """;
//                    super( new Key_id(0)
//                         , new Key_name(1)
             var templateBeg2 = """
                         );
                }
                @Override
                public ${domainClass} newDomain(@NotNull final Object... values) {
                    final var result = new ${domainClass}();
                    for (int i = 0, max = Math.min(values.length, keyList.size()); i < max; i++) {
                        final var key = (Key<${domainClass}, Object>) keyList.get(i);
                        key.setValue(result, values[i]);
                    }
                    return result;
                }
            
                @NotNull
                public Class<${domainClass}> getDomainType() {
                    return domainType;
                }
                """;

        var templateMid = """           
                /** Key id */
                static final class Key_id extends AbstractKey<${domainClass}, Long> {
                    public Key_id(int order) {
                        super(order, "id", Long.class, "id", true, true);
                    }
                    @Override
                    public void setValue(@NotNull final ${domainClass} bean, @Nullable final Long value) {
                        bean.setId(value != null ? value : defaultValue);
                    }
                    @Override
                    public Long getValue(@NotNull final ${domainClass} bean) {
                        return bean.getId();
                    }
                    @Override
                    public @NotNull Class<${domainClass}> getDomainType() {
                        return domainType;
                    }
                }
                """;
        var templateEnd = "}";

        MessageService.formatMsg(templateBeg1, params, writer);
        buildConstructor(meta, writer);
        MessageService.formatMsg(templateBeg2, params, writer);
        buildInnerKeys(templateMid, meta, writer);
        MessageService.formatMsg(templateEnd, params, writer);
        return writer.toString();
    }

    private void buildConstructor(DomainModel meta, StringWriter writer) {
        for(int i = 0, max = meta.properties().size(); i < max; ++i) {
            var row = i == 0
                    ? "super( new Key_%s(%s)"
                    : "     , new Key_%s(%s)";
            writer.append("        ")
                    .append(row.formatted(meta.properties().get(i), i))
                    .write("\n");

        }
    }

    private void buildInnerKeys(String template, DomainModel meta, StringWriter writer) {
        final var params = new HashMap<String, Object>();
        for (var prop: meta.properties()) {
            {
                params.put("propName", prop.propertyName());
                params.put("columnName", prop.dbColumName());
                params.put("type", prop.propertyType());
                params.put("domainClass", meta.beanClass().getName());
            }
            MessageService.formatMsg(template, params, writer);
        }
    }


}
