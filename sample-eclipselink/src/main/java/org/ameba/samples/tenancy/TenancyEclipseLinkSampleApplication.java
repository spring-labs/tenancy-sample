/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ameba.samples.tenancy;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * A TenancySampleApplication.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = TenancyEclipseLinkSampleApplication.class,
        entityManagerFactoryRef = "entityManagerFactory")
@RestController(value = TenancyEclipseLinkSampleApplication.ROOT_ENTRY)
public class TenancyEclipseLinkSampleApplication {

    public static final String ROOT_ENTRY = "/catalogs";
    @Autowired
    private CatalogRepository repo;

    public static void main(String[] args) {
        SpringApplication.run(TenancyEclipseLinkSampleApplication.class, args);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<CatalogEO> get() {
        return repo.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody CatalogEO catalog) {
        repo.save(catalog);
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factory.setPackagesToScan(TenancyEclipseLinkSampleApplication.class.getPackage().getName());
        factory.getJpaPropertyMap().put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_DATABASE_GENERATION);
        factory.getJpaPropertyMap().put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
        factory.getJpaPropertyMap().put(PersistenceUnitProperties.WEAVING, "false");
        factory.getJpaPropertyMap().put(PersistenceUnitProperties.MULTITENANT_SHARED_EMF, "false");
        factory.getJpaPropertyMap().put(TenantHolder.TENANT_ID, TenantHolder.getTenant());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
