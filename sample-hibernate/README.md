Multi-Tenancy Sample with Hibernate
=====================

This sample application shows how to configure Hibernate to use the Discriminator separation strategy in a multi tenant
environment.

## Further Readings

| Desc | Link |
|:---- |:---- |
| [User Guide][UG] | http://docs.jboss.org/hibernate/orm/5.0/userGuide/en-US/html_single/#d5e3197 |

## Known Issues

| Desc | Link |
|:---- |:---- |
| Current state of dev. | https://hibernate.atlassian.net/browse/HHH-6054 |

## Technologies and frameworks

- A patched version of Hibernate 5.0.4

## How it should work

When you consult the documentation you'll see that it is just a matter of configuring the `SessionFactory`
(resp. `EntityManagerFactory`) to setup Discriminator column value mapping based on tenant information. Indeed it makes
me wonder how it is possible to separate entities between tenants, because this must be configured on entity level instead.
The current documentation of Hibernate suggests that the demanded feature is planned in version 5.0:

> **DISCRIMINATOR**
  Correlates to the partitioned (discriminator) approach. It is an error to attempt to open a session without a tenant
  identifier using this strategy. This strategy is not yet implemented in Hibernate as of 4.0 and 4.1. Its support is planned
  for 5.0.

## How to configure

In combination with Spring Data JPA you only need to configure the `LocalContainerEntityManagerFactoryBean` that is
responsible to create an `EntityManagerFactory` where instances of `EntityManager`s are retrieved from. In a Spring Boot
application you should override the provided `LocalContainerEntityManagerFactoryBean` bean definition as follows:

```java
    public
    @Bean
    EntityManagerFactory customEntityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // turn off for DISCRIMINATOR strategy!
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

As the [User Guide][UG] points out, this should be enough to separate data by tenant information. But this is only the
part of configuration that enables the strategy.

| Parameter | Desc |
|:---- |:---- |
| `hibernate.multiTenancy` | Enum type to name the strategy that is used for multitenancy (NONE|SCHEMA|DATABASE|**DISCRIMINATOR**) |
| `hibernate.tenant_identifier_resolver` | FQN class name or class instance of the resolver to use the tenant id from |

With Hibernate no extra metadata on entity classes is foreseen so far. This means requirement [R1] (../README.md) can
not be fulfilled.

## Runtime Behavior

With the configuration above, the application starts up, but whenever you try to execute a database call it will fail
gracefully with a NPE:

```java
java.lang.NullPointerException: null
	at org.hibernate.internal.AbstractSessionImpl$ContextualJdbcConnectionAccess.obtainConnection(AbstractSessionImpl.java:425) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded(LogicalConnectionManagedImpl.java:87) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.getPhysicalConnection(LogicalConnectionManagedImpl.java:112) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
	at org.hibernate.internal.SessionImpl.connection(SessionImpl.java:488) ~[hibernate-core-5.0.4.Final.jar:5.0.4.Final]
```

## Patch Required

Hibernate tries to obtain a JDBC connection from a `MultiTenantConnectionProvider`. But this fails because a
`MultiTenantConnectionProvider` is not the suited `ConnectionProvider` implementation for the Discriminator Column Value
Based strategy (DCVB). Separating user data based on a discriminator column value does not depend on the underlying JDBC
connection. In contrast, a Database or Schema separating strategy do rely on the JDBC connection, because the connection
must be properly setup to point to the right database or database schema. We need to apply a patch to the
`AbstractSessionImpl` to get this fixed:

```java
@@ -339,7 +339,8 @@
 	@Override
 	public JdbcConnectionAccess getJdbcConnectionAccess() {
 		if ( jdbcConnectionAccess == null ) {
-			if ( MultiTenancyStrategy.NONE == factory.getSettings().getMultiTenancyStrategy() ) {
+			if ( MultiTenancyStrategy.NONE == factory.getSettings().getMultiTenancyStrategy() ||
+                    MultiTenancyStrategy.DISCRIMINATOR == factory.getSettings().getMultiTenancyStrategy()) {
 				jdbcConnectionAccess = new NonContextualJdbcConnectionAccess(
 						getEventListenerManager(),
 						factory.getServiceRegistry().getService( ConnectionProvider.class )
```

When you go through the source code of Hibernate you often find these comparisons against the `MultiTenancyStrategy`.
Seems like multitenancy support is already considered at many places. But in fact it is not implemented finally.

## Whats missing?

With the patch above, we can now startup our sample, insert some test data and fetch data from the database:

Send a POST request with request body `{"version":"7.2"}` to create a new `CatalogEO`.
```
insert into T_CATALOG (C_VERSION, C_PK) values ('7.2', 27354)
```
Hibernate is now calling our custom implementation of `org.hibernate.context.spi.CurrentTenantIdentifierResolver` to
get the tenant identifier but does not consider it in the INSERT statement. For sure not, because we haven't
configured any discriminator column.

So what is the tenant identifier used for? It is stored in each `Session` instance and is only used by the
`ConnectionProvider` to obtain a connection from the proper database. That means the DCVB feature is not implemented
yet. The initial JIRA task is till opened and it seems like there is still some amount of design work to do before the
implementation can go on. IMO the documentation of Hibernate 5.0 should clarify the state of development.

[UG]: http://docs.jboss.org/hibernate/orm/5.0/userGuide/en-US/html_single/#d5e3197  "Hibernate.org User Guide"