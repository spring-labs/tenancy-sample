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

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
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
@EnableJpaRepositories(basePackageClasses = TenancySampleApplication.class,
        entityManagerFactoryRef = "entityManagerFactory")
@RestController(value = TenancySampleApplication.ROOT_ENTRY)
public class TenancySampleApplication {

    public static final String ROOT_ENTRY = "/catalogs";
    @Autowired
    private CatalogRepository repo;

    public static void main(String[] args) {
        SpringApplication.run(TenancySampleApplication.class, args);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<CatalogEO> get() {
        return repo.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody CatalogEO catalog) {
        repo.save(catalog);
    }

    public
    @Bean
    EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(false);
        vendorAdapter.setDatabasePlatform(H2Dialect.class.getName());

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(TenancySampleApplication.class.getPackage().getName());
        factory.setDataSource(dataSource);
        factory.getJpaPropertyMap().put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.DISCRIMINATOR);
//        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, new DataSourceBasedMultiTenantConnectionProviderImpl()/*CustomMultiTenantConnectionProvider.class.getName()*/);
        //factory.getJpaPropertyMap().put(DataSourceBasedMultiTenantConnectionProviderImpl.TENANT_IDENTIFIER_TO_USE_FOR_ANY_KEY, "Default");
        //factory.getJpaPropertyMap().put(AvailableSettings.DATASOURCE, dataSource);

        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, new TenantHolder());
        factory.afterPropertiesSet();

        return factory.getObject();
    }
}
