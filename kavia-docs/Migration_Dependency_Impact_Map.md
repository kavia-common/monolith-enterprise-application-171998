# Migration Dependency & Impact Assessment: Snowman Backend Java 21 Upgrade

## Overview

This document analyzes all critical dependencies and their potential impact for upgrading the Snowman backend from legacy Java (1.7) to Java 21 LTS. It includes a version mapping table, direct upgrade implications, action points, and commands to assist maintainers in planning and executing the migration safely.

---

## Current & Target Dependency Matrix

| Area           | Dependency         | Current Version                  | Target Version for Java 21        | Notes & Upgrade Considerations                             |
|----------------|--------------------|----------------------------------|-----------------------------------|-----------------------------------------------------------|
| Java SDK       | Java               | 1.7                              | 21                                | Must update POM compiler plugin & codebase for new APIs   |
| Build Tool     | Maven              | (not specified in POM, assume 3.x) | 3.9.x+                            | Maven 3.9+ recommended for Java 21                        |
| Container      | Jetty              | 9.4.11.v20180605                 | 12.x (Jakarta EE)/11.x (Servlet 4)| Jetty 12 moves to Jakarta EE; massive API changes         |
| Web/Framework  | Spring Framework   | [4.3.18, )                       | 6.x                               | Java 17+ required in 6.x; Spring 5.3 (min) for JDK 17     |
| ORM            | Hibernate Core     | 5.4.24.Final                     | 6.3.x+/6.4.x                      | Java 21 compatibility in 6.2+, annotations/package changes|
| ORM Add-on     | hibernate-entitymanager | 4.3.10.Final                  | REMOVE (merged in 5+)             | Remove; use hibernate-core only                           |
| ORM Add-on     | hibernate-c3p0     | 4.3.10.Final                     | Remove/Upgrade                    | Review, prefer HikariCP                                   |
| Database       | mysql-connector-java| 8.0.16                          | 8.2.x                             | Update for SSL, time zone, driver class changes           |
| Migration      | Liquibase          | 3.0.5                            | 4.23+/4.24+                       | 3.x not compatible with Java 17+                          |
| Messaging      | ActiveMQ           | 5.10.0 (activemq-spring)         | 5.18.x+                           | JMS package changes, new client APIs                      |
| Cache          | Ehcache            | 2.9.1                            | 3.10.x                            | API and config structure changed, major breaking changes  |
| Logging        | slf4j-api          | 1.7.25                           | 2.x                               | Check all bridges/bindings, some interfaces changed       |
| Testing        | JUnit              | 4.13.1                           | 5.10.x                            | Refactor tests for JUnit Jupiter                         |
| Testing        | Mockito            | 1.10.19                          | 5.x                               | Versions <4 won't run on Java 17+                         |
| Testing        | PowerMock          | 1.7.3                            | Avoid/migrate                     | PowerMock may break with modern Mockito/JUnit             |
| Bean Validation| Hibernate Validator| 5.4.2.Final                      | 8.x                               | EE annotations now use Jakarta.*                          |

> **Note:** To generate a precise current-versus-available plugin and dependency report, use:

```sh
mvn versions:display-plugin-updates
mvn versions:display-dependency-updates
```
For each indicated upgrade, refer to the relevant project documentation for Java 21 compatibility.

---

## Major Breaking Change Highlights

- **Jakarta EE 9 transition:** javax.* APIs move to jakarta.*; impacts Spring, Hibernate, Jetty, and beans validation.
- **Spring Framework 6+:** Baseline is Java 17+, moves to jakarta.* APIs.
- **Hibernate 6.x+:** Drops legacy APIs; JPA moved under jakarta.persistence.*.
- **Liquibase:** v3.x won't work on Java 17+; upgrade to 4.23+ required.
- **Ehcache:** v3 uses a completely new configuration syntax (XML/Java).
- **ActiveMQ:** Verify JMS/JCA compatibility, prefer modern Spring JMS abstractions.
- **Jetty 12+:** Only supports Jakarta EE APIs.
- **JUnit 5:** Migrate tests and runners (annotations and extension model).

---

## Impacted Files/Configs

- `pom.xml`: Must update all dependency and plugin versions, and Maven compiler plugin source/target.
- `src/main/resources/webapp/WEB-INF/web.xml`: Remove `javax.` API usage where possible, adjust servlets.
- `src/main/resources/webapp/WEB-INF/ehcache.xml`: Reconfigure for Ehcache 3+ if retaining.
- `src/main/resources/application.properties`: Check for JDBC/ORM driver changes.
- All Java sources: Replace deprecated or removed APIs (javax.*), refactor tests, update package imports.

---

## Actionable Checklist

- [ ] Audit all usages of javax.* and adapt to jakarta.*.
- [ ] Run the upgrade commands to get live Maven version reality check.
- [ ] Update all plugins in POM to latest LTS (see table above for guidance).
- [ ] Identify direct/indirect usage of deprecated APIs and build breaking features.
- [ ] Test with `mvn clean verify` after each big upgrade.

**Sources**:  
- pom.xml  
- web.xml  
- ehcache.xml  
- application.properties  
