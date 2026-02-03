/* Copyright (c) 2018-2026 Pavel Ponec <help.ujorm@gmail.com> */
package org.ujorm.wicket.component.grid;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;

/**
 * Optimized Data provider for a better performance on the paging
 * @author Pavel Ponec
 */
public class OrmDataProviderCached<U extends OrmUjo> extends OrmDataProvider<U> {

    /** Duration of data cache */
    @NotNull
    private static final Duration dataLife = Duration.ofMinutes(2);

    /** Duration of data cache */
    private LocalDateTime nextUpdate = LocalDateTime.now();

    /** Duration of data cache */
    private int lastFilterHash = 0;

    /** Max page count */
    protected final int maxPages = 9;

    /** Default value */
    private int rowsPerPage = 10;

    /** Row cache */
    private final List<U> rows = new ArrayList<>(rowsPerPage);

    /** Consturctor */
    public OrmDataProviderCached(@NotNull final IModel<Criterion<U>> criterion) {
        super(criterion);
    }

    /** Consturctor */
    public OrmDataProviderCached(@NotNull final IModel<Criterion<U>> criterion, @NotNull final Key<? super U, ?> defaultSort) {
        super(criterion, defaultSort);
    }

    /** Method calculate the size using special SQL request.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    @Override
    public long size() {
        final LocalDateTime now = LocalDateTime.now();
        final Criterion<U> crn = super.filter.getObject();
        final int newHash = calculageHash(crn);

        if (now.isAfter(nextUpdate) || lastFilterHash != newHash) {
            nextUpdate = now.plus(getDataLife());
            lastFilterHash = newHash;
            refreshRows(crn);
        }
        return rows.size();
    }

    /** Calcualte hash of the criterion */
    private int calculageHash(final @Nullable Criterion<U> crn) {
        final Key sortKey = super.getSortKey();
        final int newHash = (crn != null ? crn.hashCode() : 0) * 51 + sortKey.hashCode();
        return newHash;
    }

    /** Refresh rows */
    private void refreshRows(@NotNull final Criterion<U> crn) {
        rows.clear();
        final Iterator<U> it = iterator(0L, getRowCountLimit(), crn);
        while(it.hasNext()) {
            rows.add(it.next());
        }
    }

    /** Get a sublist. */
    @Override @NotNull
    public Iterator<U> iterator(final long first, final long count) {
        return rows.subList((int)first, (int)(first + count)).iterator();
    }

    /** Create AJAX-based DataTable with a {@link #DEFAULT_DATATABLE_ID} */
    @Override
    public <S> DataTable<U,S> createDataTable(final int rowsPerPage) {
        final DataTable<U,S> result = createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage);
        this.rowsPerPage = rowsPerPage;
        return result;
    }

    /** Create AJAX-based DataTable */
    @Override
    public final <S> DataTable<U,S> createDataTable(final String id, final int rowsPerPage) {
        final DataTable<U,S> result = createDataTable(id, rowsPerPage, false);
        this.rowsPerPage = rowsPerPage;
        return result;
    }

    /** Create AJAX-based DataTable */
    @Override
    public final <S> DataTable<U,S> createDataTable(final int rowsPerPage, final boolean insertToolbar) {
        final DataTable<U,S> result = createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage, insertToolbar);
        this.rowsPerPage = rowsPerPage;
        return result;
    }

    /** Cache duration is 2 minutes by default */
    @NotNull
    public Duration getDataLife() {
        return dataLife;
    }

    /** Get max row count limit */
    protected int getRowCountLimit() {
        return rowsPerPage * maxPages;
    }

    /** Clerar cache, if any */
    @Override
    public void clearCache() {
        nextUpdate = LocalDateTime.now();
    }

    // ============= STATIC METHOD =============

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(@NotNull final IModel<Criterion<T>> criterion, Key<? super T,?> defaultSort) {
        return new OrmDataProviderCached<T>(criterion, defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(@NotNull final IModel<Criterion<T>> criterion) {
        return new OrmDataProviderCached<T>(criterion, null);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(@NotNull final Criterion<T> criterion, Key<? super T,?> defaultSort) {
        return new OrmDataProviderCached<T>(new Model(criterion), defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(Criterion<T> criterion) {
        return new OrmDataProviderCached<T>(new Model(criterion), null);
    }

}
