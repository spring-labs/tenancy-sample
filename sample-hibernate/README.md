Multi-Tenancy Sample with Hibernate
=====================

This sample application shows how to configure Hibernate to use the Discriminator separation strategy in a multitenancy
environment.

## Further Readings

| Desc | Link |
|:---- |:---- |
| User Guide | http://docs.jboss.org/hibernate/orm/5.0/userGuide/en-US/html_single/#d5e3197 |

## Known Issues

| Desc | Link |
|;---- |:---- |
|Current state of dev. | https://hibernate.atlassian.net/browse/HHH-6054 |

## Technologies and frameworks used:

- A patched version of Hibernate 5.0.4

## How it should work

When you consult the documentation you'll see that it is just a matter of configuring the SessionFactory
(resp. EntityManagerFactory) to setup Discriminator column value mapping based on tenant information. Indeed that makes
me wonder how it is possible to separate between entities between tenants. Furthermore the current documentation of Hibernate
suggests that the demanded feature may exist in version 5.0 (but it does not).

> **DISCRIMINATOR**
  Correlates to the partitioned (discriminator) approach. It is an error to attempt to open a session without a tenant
  identifier using this strategy. This strategy is not yet implemented in Hibernate as of 4.0 and 4.1. Its support is planned
  for 5.0.

## How to configure

In combination with Spring Data JPA you only need to configure the `LocalContainerEntityManagerFactoryBean` that is
responsible to create an `EntityManagerFactory` where instances of `EntityManager`s are retrieved from. In a Spring Boot
application you should define your own `LocalContainerEntityManagerFactoryBean` definition:

```java
    public
    @Bean
    EntityManagerFactory customEntityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // turn off with Discriminator strategy so far!
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(TenancySampleApplication.class.getPackage().getName());
        factory.setDataSource(dataSource);
        factory.getJpaPropertyMap().put(Environment.DIALECT, PostgreSQL9Dialect.class.getName());
        factory.getJpaPropertyMap().put(Environment.MULTI_TENANT, MultiTenancyStrategy.DISCRIMINATOR);
        factory.getJpaPropertyMap().put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, new TenantHolder());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
```

As the [User Guide[User Guide]] points out, this should be enough to separate data by tenant information. But this is only the
configuration part. What do we need to configure in detail especially on our entity classes ?

| Parameter | Desc |
|:---- |:---- |
| hibernate.multiTenancy | Enum type to name the strategy that is used for multitenancy |
| hibernate.tenant_identifier_resolver | FQN class name or class instance of the resolver to use the tenant id from |

Currently no extra metadata on entity classes with the Hibernate solution is foreseen, that means that requirement [R1]
(../README.md) can not be fulfilled.

## Runtime Behavior

With the configuration above, the application will startup, but whenever you try to execute a database call it will fail
gracefully with a NPE:

```java
java.lang.NullPointerException: null
	at org.hibernate.internal.AbstractSessionImpl$ContextualJdbcConnectionAccess.obtainConnection(AbstractSessionImpl.java:425) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded(LogicalConnectionManagedImpl.java:87) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.getPhysicalConnection(LogicalConnectionManagedImpl.java:112) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.internal.SessionImpl.connection(SessionImpl.java:488) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
```

