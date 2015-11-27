Multi-Tenancy Sample with EclipseLink
=====================

This sample application shows how to configure EclipseLink to use the Discriminator separation strategy in a multi tenant
environment.

## Further Readings

| Desc | Link |
|:---- |:---- |
| Solutions Guide, ch.14 | http://www.eclipse.org/eclipselink/documentation/2.6/solutions/multitenancy.htm#CHDBJCJA |

## Technologies and frameworks used:

- EclipseLink 2.6.1

## How it works
Configuration and usage of the Discriminator Column Value Based strategy (DCVB) is straightforward. At first we have to
annotate all entity classes that are *tenant aware* with an `@Multitenant` annotation. This annotation allows use to
define a separation strategy. The default strategy `MultitenantType.SINGLE_TABLE` fits our requirements. As next we've
to tell EclipseLink the name of the discriminator column to use. This can be done in several ways, we're fine with
putting more EclipseLink specific annotations on our entity classes:

```java
@TenantDiscriminatorColumn(name = "C_TENANT_ID", contextProperty = TenantHolder.TENANT_ID)
```

Worth to notice is the `contextProperty` attribute of the annotation. This tells EclipseLink to take the tenant identifier
information from the configured JPA properties that are used to create the `EntityManagerFactory` at application _startup_.

```java
    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        ...
        factory.getJpaPropertyMap().put(TenantHolder.TENANT_ID, TenantHolder.getTenant());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
```

Further details of the configuration parameters in `org.eclipse.persistence.config.PersistenceUnitProperties` can be
taken from the Javadocs.

## Restrictions

At application _startup_. This is not what we want. We need to pass the current tenant dynamically to EclipseLink
depending on the tenant identifier we get with each http request. One can argue, that we can create new `EntityManagerFactory`s
on the fly, cache them within a map and grab the one for the current tenant. This would work in a plain JPA environment,
but as we're using Spring Data JPA, we need to pass the correct `EntityManagerFactory` to Spring Data at application
startup in order to build the repositories properly.

```java
@EnableJpaRepositories(basePackageClasses = TenancyEclipseLinkSampleApplication.class,
        entityManagerFactoryRef = "entityManagerFactory")
```

## Runtime Behavior

Let's create a new `CatalogEO` by sending a POST request with request body `{"version":"7.2"}`.

```
INSERT INTO T_CATALOG (C_PK, C_VERSION, C_TENANT_ID) VALUES (?, ?, ?)
	bind => [151, 7.2, FIXME]
```

EclipseLink recognizes that our entity class is tenant aware and adds the tenant identifier to the insert statement. The
same is true when getting back the data from the database.

```
SELECT C_PK, C_TENANT_ID, C_VERSION FROM T_CATALOG WHERE (C_TENANT_ID = ?)
	bind => [FIXME]
```

