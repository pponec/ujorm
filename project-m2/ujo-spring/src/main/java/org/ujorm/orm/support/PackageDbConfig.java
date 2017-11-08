/*
 *  Copyright 2017-2017 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.orm.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoDecorator;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyRing;
import org.ujorm.extensions.NativeUjoDecorator;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.annot.Db;
import org.ujorm.tools.Assert;

/**
 * Get all classes from the {@code  dbClass} package.
 * @author Pavel Ponec
 * @see org.ujorm.extensions.StringWraper
 */
public class PackageDbConfig<U extends OrmUjo> extends NativeUjoDecorator<U> {

    @Nonnull
    protected final KeyList<U> keyList;

    /**
     * Constructor
     * @param <U>
     * @param dbClass
     * @throws IllegalArgumentException At least one table is needed
     */
    public <U extends OrmUjo> PackageDbConfig(@Nonnull final Class<U> dbClass) throws IllegalArgumentException{
        super(dbClass);
        final List result = new ArrayList<>();
        final List<Class> tables = findTables();
        Collections.sort(tables, (Class c1, Class c2) -> c1.getSimpleName().compareTo(c2.getSimpleName()));
        for (int i = 0, max = tables.size(); i < max; i++) {
            final Class type = tables.get(i);
            result.add(new RelationToMany(type.getSimpleName(), dbClass, type, i, true));
        }
        Assert.hasLength(result, "At least one table is needed");
        this.keyList = KeyRing.of(dbClass, result);
    }

    @Override @Nonnull
    public KeyList<U> getKeys() {
        return keyList;
    }

    /** Return a list of the packages */
    private Set<Package> getPackages() {
        final KeyList<U> tableList = domain.readKeys();
        final Set<Package> result = new HashSet(1 + tableList.size());

        if (tableList.isEmpty()) {
            result.add(domain.getClass().getPackage());
        } else {
            for (Key<U, Object> key : tableList) {
                if (key instanceof RelationToMany) {
                    result.add(((RelationToMany)key).getItemType().getPackage());
                }
            }
        }
        return result;
    }

    /** Find all table from required package */
    @Nonnull
    private List<Class> findTables() {
        try {
            final List<Class> result = new ArrayList<>();
            for (Package p : getPackages()) {
                final String basePackage = "/" + p.getName().replace('.', '/');
                final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
                final String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + resolveBasePackage(basePackage) + "/*.class";
                final Resource[] resources = resolver.getResources(locationPattern);

                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        final MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                        final Class<?> clazz = Class.forName(reader.getClassMetadata().getClassName());
                        if (isCandidate(clazz)) {
                            result.add(clazz);
                        }
                    }
                }
            }
            return result;
        } catch (IOException | ReflectiveOperationException | RuntimeException e) {
            throw new IllegalUjormException(e.getMessage(), e);
        }
    }

    /** Resolve Base Package */
    private String resolveBasePackage(@Nonnull final String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /** Check a candidate */
    protected boolean isCandidate(@Nonnull final Class<?> clazz) throws ReflectiveOperationException {
        if (!OrmTable.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (clazz.getAnnotation(Db.class) != null) {
            return false;
        }
        if (domain.getClass().equals(clazz)) {
            return false;
        }
        return true;
    }

    /** Create new instance */
    public static <U extends Ujo> UjoDecorator<U> of(@Nonnull final Class<U> dbClass) {
        return new PackageDbConfig(dbClass);
    }
}
