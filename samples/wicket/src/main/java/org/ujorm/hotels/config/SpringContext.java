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
package org.ujorm.hotels.config;

import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.ujorm.hotels.gui.MainApplication;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.hotels.service.impl.DbServiceImpl;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.spring.CommonDao;
import org.ujorm.spring.UjormTransactionManager;

/**
 * Konfigurace Springového contextu modulu WS
 * @author Pavel Ponec
 * @see https://www.tutorialspoint.com/spring/spring_java_based_configuration.htm
 */
@Configuration
@EnableTransactionManagement()
@ComponentScan(basePackageClasses = {ParamService.class, MainApplication.class}
        , lazyInit = true)
public class SpringContext {

    /** Name of the transaction manager */
    public static final String TRANSACTION_MANAGER = "ujormTransactionManager";
    /** Name of the ORM handler */
    public static final String ORM_HANDLER = "ormHandler";

    /** A configuration provider */
    @Bean(name = SpringContext.ORM_HANDLER, initMethod = "init")
    public OrmHandlerProvider ormHandlerProvider() {
        return new DatabaseConfig();
    }

    /** Transaction Manager */
    @Bean(name = SpringContext.TRANSACTION_MANAGER)
    public UjormTransactionManager txManager() {
        final UjormTransactionManager result = new UjormTransactionManager();
        result.setOrmHandlerProvider(ormHandlerProvider());
        return result;
    }

    /** Common DAO object */
    @Bean
    public CommonDao commonDao() {
        return new CommonDao(txManager());
    }

    /** Configuration of the DemoHotels application */
    @Bean
    public DbServiceImpl dbServiceImpl() {
        final DbServiceImpl result = new DbServiceImpl();
        result.setReadOnly(false);
        result.setMeasuringCode(false);
        return result;
    }

    @Bean("cacheManager")
    public EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean result = new EhCacheManagerFactoryBean();
        result.setConfigLocation(new ClassPathResource("/ehcache.xml"));
        result.setShared(true);
        return result;
    }
}