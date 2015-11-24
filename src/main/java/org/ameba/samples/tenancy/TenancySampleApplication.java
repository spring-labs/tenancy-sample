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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@EnableJpaRepositories(basePackageClasses = TenancySampleApplication.class)
@RestController(value = TenancySampleApplication.ROOT_ENTRY)
public class TenancySampleApplication {

    public static final String ROOT_ENTRY = "/catalogs";
    public static void main(String[] args) {
        SpringApplication.run(TenancySampleApplication.class, args);
    }

    @Autowired
    private CatalogRepository repo;

    @RequestMapping(method = RequestMethod.GET)
    public List<CatalogEO> get() {
        return repo.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody CatalogEO catalog){
        repo.save(catalog);
    }

    public
    //@Bean
    EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(TenancySampleApplication.class.getPackage().getName());
        factory.setDataSource(dataSource);
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.multiTenancy", "DISCRIMINATOR");
        jpaProperties.put("hibernate.tenant_identifier_resolver", TenantHolder.class.getName());
        factory.setJpaPropertyMap(jpaProperties);
        factory.afterPropertiesSet();

        return factory.getObject();
    }
}
