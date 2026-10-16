package org.ujorm.orm.converter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/** An entity with explicit converters */
@Getter @Setter
@Table(name = "customer")
public class Customer {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Nullable
    @Convert(converter = EmailConverter.class)
    private Email email;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "active", nullable = false)
    private boolean active;

    /** Native boolean without a converter */
    private Boolean newsletter;

    @Convert(converter = CustomerState.Converter.class)
    @Column(nullable = false)
    private CustomerState state = CustomerState.ACTIVE;

    /** Create new instance without ID */
    public static Customer of(String name, @Nullable String email, boolean active, CustomerState state) {
        var result = new Customer();
        result.setName(name);
        result.setEmail(email != null ? new Email(email) : null);
        result.setActive(active);
        result.setNewsletter(true);
        result.setState(state);
        return result;
    }
}
