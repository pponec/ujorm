package org.ujorm2.core;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.ujorm2.CompositeKey;
import org.ujorm2.Key;
import org.ujorm2.ListKey;
import org.ujorm2.Validator;
import org.ujorm2.criterion.Criterion;
import org.ujorm2.criterion.Operator;
import org.ujorm2.criterion.ProxyValue;
import org.ujorm2.validator.ValidationException;

/**
 *
 * @author Pavel Ponec
 */
public class AbstractKey<D, V> implements Key<D, V>, MetaInterface<D> {

    /** Context of the Ujorm */
    @Nonnull
    private final UjoContext context;

    /** Domain class */
    @Nonnull
    private final Class<D> domainClass;

    public AbstractKey(Class<D> domainClass, UjoContext context) {
        this.context = context != context ? context : UjoContext.of();
        this.domainClass = domainClass;
    }

    /** Context of the Ujorm */
    protected final UjoContext getContext() {
        return context;
    }

    /** Domain class */
    @Override
    public final Class<D> getDomainClass() {
        return domainClass;
    }

    @Override
    public D createDomain() throws IllegalStateException {
        try {
            return getDomainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFullName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<V> getValueClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setValue(V value, D ujo) throws ValidationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V getValue(D ujo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V of(D ujo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V getDefaultValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDefault(D ujo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(D ujo, V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equalsName(CharSequence name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isComposite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTypeOf(Class type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDomainOf(Class type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAscending() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Key<D, V> descending() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Key<D, V> descending(boolean descending) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Validator<V> getValidator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> CompositeKey<D, T> join(Key<? super V, T> key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> CompositeKey<D, T> join(Key<? super V, T> key, String alias) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> ListKey<D, T> join(ListKey<? super V, T> key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(D from, D to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(Key p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toStringDetailed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int length() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char charAt(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forCriterion(Operator operator, V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forCriterion(Operator operator, ProxyValue<V> proxyValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forCriterion(Operator operator, Key<?, V> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forEq(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forEq(ProxyValue<V> proxyValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forEq(Key<D, V> key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forIn(Collection<V> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNotIn(Collection<V> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forIn(V... list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNotIn(V... list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNeq(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forGt(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forGe(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forLt(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forLe(V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNull() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNotNull() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forSql(String sqlCondition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forSql(String sqlTemplate, V value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forSqlUnchecked(String sqlTemplate, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Criterion<D> forNone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
