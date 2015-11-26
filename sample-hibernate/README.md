Multi-Tenancy Sample with Hibernate
=====================

This sample application shows how to configure Hibernate to use the Discriminator separation strategy in a multitenancy
environment.

## Further Readings

| Desc | Link |
| ---- | ----:|
| User Guide | http://docs.jboss.org/hibernate/orm/5.0/userGuide/en-US/html_single/#d5e3197 |

## Known Issues

| Desc | Link |
| ---- | ----:|
|Current state of dev. | https://hibernate.atlassian.net/browse/HHH-6054 |

## Technologies and frameworks used:

- A patched version of Hibernate 5.0.4

## How it should work

When you consult the documentation you'll see that it is just a matter of configuring the SessionFactory
(resp. EntityManagerFactory) to setup Discriminator column value mapping based on tenant information. Indeed that makes
me wonder how it is possible to separate between entities between tenants. Furthermore the current documentation of Hibernate
suggests that the demanded feature may exist in version 5.0 (but it does not).

> DISCRIMINATOR
  Correlates to the partitioned (discriminator) approach. It is an error to attempt to open a session without a tenant identifier using this strategy. This strategy is not yet implemented in Hibernate as of 4.0 and 4.1. Its support is planned for 5.0.

