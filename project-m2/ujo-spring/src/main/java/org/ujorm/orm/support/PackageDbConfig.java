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
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyRing;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.NativeDbConfig;
import org.ujorm.orm.OrmUjo;
import org.ujorm.tools.Assert;

/**
 * Get all classes from the {@code  dbClass} package.
 * @author Pavel Ponec
 * @see org.ujorm.extensions.StringWraper
 */
public class PackageDbConfig<U extends OrmUjo> extends NativeDbConfig<U> {

    @Nonnull
    private final KeyList<U> keyList;

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
        int i = 0;
        for (Class type : tables) {
            result.add(new RelationToMany(type.getSimpleName(), dbClass, type, i++, true));
        }
        Assert.hasLength(result, "At least one table is needed");
        this.keyList = KeyRing.of(dbClass, result);
    }

    @Override @Nonnull
    public KeyList<U> getTableList() {
        return keyList;
    }

    /** Return a list of the packages */
    private Set<Package> getPackages() {
        final KeyList<U> tableList = super.getTableList();
        final Set<Package> result = new HashSet(1 + tableList.size());

        if (tableList.isEmpty()) {
            result.add(getClass().getPackage());
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

                for (org.springframework.core.io.Resource resource : resources) {
                    if (resource.isReadable()) {
                        final MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        if (isCandidate(metadataReader)) {
                            result.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                        }
                    }
                }
            }
            return result;
        } catch (IOException | ReflectiveOperationException | RuntimeException e) {
            throw new IllegalUjormException(e.getMessage(), e);
        }
    }

    private String resolveBasePackage(@Nonnull final String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    private boolean isCandidate(@Nonnull final MetadataReader reader) throws ReflectiveOperationException {
        final Class c = Class.forName(reader.getClassMetadata().getClassName());
        if (!OrmTable.class.isAssignableFrom(c)) {
            return false;
        }
        if (c.getAnnotation(OrmTable.class) != null) {
            return false;
        }
        if (getDbModel().getClass().equals(c)) {
            return false;
        }
        return true;
    }

}
