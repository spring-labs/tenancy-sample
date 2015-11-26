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
    public JpaVendorAdapter jpaVendorAdapter() {
    	EclipseLinkJpaVendorAdapter jpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.H2);
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabasePlatform("org.eclipse.persistence.platform.database.H2Platform");
        return jpaVendorAdapter;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(this.jpaVendorAdapter());
        emf.setPackagesToScan(TenancyEclipseLinkSampleApplication.class.getPackage().getName());
        emf.setJpaProperties(this.eclipseLinkProperties());
        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Bean
    public JpaDialect jpaDialect() {
        return new EclipseLinkJpaDialect();
    }
    
    @Bean
    public Properties eclipseLinkProperties() {
        Properties eclipseLinkProps = new Properties();
        //eclipseLinkProps.setProperty("eclipselink.ddl-generation", "drop-and-create-tables");
        eclipseLinkProps.setProperty("eclipselink.ddl-generation.output-mode", "database");
        eclipseLinkProps.setProperty("eclipselink.weaving", "false");
        eclipseLinkProps.setProperty("eclipselink.logging.level", "FINEST");
        // flag for shared entity manager factory.
        eclipseLinkProps.setProperty("eclipselink.multitenant.tenants-share-emf", "false");
        eclipseLinkProps.setProperty("tenant.id", "LEA");
        return eclipseLinkProps;
    }

}
