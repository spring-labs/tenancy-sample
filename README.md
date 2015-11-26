Tenancy Sample
=====================

A sample application to test Spring Data GOSLING with Hibernate 5 and discriminator column separation.

This sample application shows how to setup a multitenancy application using the Discriminator column separation strategy
on relational databases. Multitenancy is still not standardized in the Java Persistence API (current version JPA 2.1) but
the common strategies are implemented by JPA providers like Hibernate or Eclipselink. Therefore we've split the sample into
two particular samples, one for each provider.

# Conclusion
After doing some research and implemented both solutions we come to the conclusion that both providers do not match our
projects requirements. The implementation of the Discriminator strategy in Hibernate is still under development and not
yet finished, even it is described in the reference documentation. We had to make changes in the current version of
Hibernate to get at least the resolution of the tenant id working. Eclipselink's Discriminator strategy implementation
is much more proven and works as described, but has some shortcomings that prevent us from using it together with the
latest Spring Data version. Details of both investigation can be found on the modules README pages.

# Technologies and frameworks used in common:
- Java 8
- Spring Boot 1.3
- Spring Data GOSLING, including Spring Data JPA
- H2 and PostgreSQL database

# Introduction in Mutlitenancy
A brief overview on multitenancy and the three mayor strategies can be found in the [Hibernate.org reference guide](http://docs.jboss.org/hibernate/orm/5.0/userGuide/en-US/html_single/#d5e3197
). Separation
on database or schema level is sufficient for most applications. Where seperating data based on a discriminator column value is
the more exotic variant. As often, it depends on your requirements on multitenany which strategy to use. XXX

# Sample App Requirements
The following requirements are taken from our Stamplets project and are applied to the sample application.

R1: The system must separate persisted data based on tenant information (sent by the client). Some data (database tables)
must be shared across all tenants.

R1.1: Log files are separated base on the tenant identifier.

R2: The system must provide the functionality to create new accounts at runtime, this implies that the system must handle
new tenants dynamically

# Variations
At first we tried to setup multitenancy data separation based on a discriminator column value with Hibernate. The
