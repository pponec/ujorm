package org.ujorm.mapper;

import lombok.Locked;
import org.ujorm.tools.msg.MessageService;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;

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
            params.put("class", meta.beanClass().getSimpleName());
            params.put("domainClass", meta.beanClass().getName());
            params.put("keys", "");
        }
        var templateBeg = """
            package org.ujorm.mapper.generated;
            import org.jetbrains.annotations.NotNull;
            import org.jetbrains.annotations.Nullable;
            import org.ujorm.core.Key;
            import org.ujorm.core.impl.AbstractKey;
            import org.ujorm.core.impl.AbstractMetaModel;
            
            public class Employee_ extends AbstractMetaModel<Employee> {
                private static final Class<Employee> domainType = Employee.class;
                public Employee_() {
                    super( new Key_id(0)
                         , new Key_name(1)
                         , new Key_city(2)
                         , new Key_superior(3)
                         , new Key_contractDay(4)
                         , new Key_active(5)
                         );
                }
            
                @Override
                public Employee newDomain(@NotNull final Object... values) {
                    final var result = new Employee();
                    for (int i = 0, max = Math.min(values.length, keyList.size()); i < max; i++) {
                        final var key = (Key<Employee, Object>) keyList.get(i);
                        key.setValue(result, values[i]);
                    }
                    return result;
                }
            
                @NotNull
                public Class<Employee> getDomainType() {
                    return domainType;
                }
                """;

        var templateMid = """           
                /** Key id */
                static final class Key_id extends AbstractKey<Employee, Long> {
                    public Key_id(int order) {
                        super(order, "id", Long.class, "id", true, true);
                    }
                    @Override
                    public void setValue(@NotNull final Employee bean, @Nullable final Long value) {
                        bean.setId(value != null ? value : defaultValue);
                    }
                    @Override
                    public Long getValue(@NotNull final Employee bean) {
                        return bean.getId();
                    }
                    @Override
                    public @NotNull Class<Employee> getDomainType() {
                        return domainType;
                    }
                }
                """;
        var templateEnd = "}";

        MessageService.formatMsg(templateBeg, params, writer);
        formatMiddle(templateMid, meta, writer);
        MessageService.formatMsg(templateEnd, params, writer);

        return writer.toString();
    }

    private void formatMiddle(String template, DomainModel meta, StringWriter writer) {
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
