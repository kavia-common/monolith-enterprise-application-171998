# Java 21 Migration Tracker

## Document Location
This document resides at: kavia-docs/Migration_Tracker.md

If you move or rename this file, update this section and any links that reference it.

## 1) Overview

### Objective
Upgrade the runtime and build to Java 21 (LTS) while keeping feature parity and ensuring compatibility across the stack.

### Scope
Applies to the monolith-enterprise-application-171998 backend, which is a Spring-based monolith using:
- Spring Framework (non-Boot)
- Hibernate/JPA
- Liquibase
- ActiveMQ (classic 5.x)
- Ehcache
- Embedded Jetty with Uber JAR packaging via Maven Shade

Primary interfaces include REST (Jetty/Servlet), JDBC (MySQL), JMS (ActiveMQ), caching, and Liquibase-driven database migrations.

## 2) Current environment

Based on repository inspection at the time of this update:

- Java version:
  - pom.xml property: <java.version>17</java.version> (targeting Java 17 currently)
  - No Maven toolchain configured; compilation uses maven-compiler-plugin defaults via project properties and plugin settings in the POM (compiler plugin not explicitly present).
- Spring Framework:
  - Version: 5.3.30 (plain Spring; not Spring Boot)
  - Spring XML config present in src/main/resources/META-INF
- MySQL driver:
  - Dependency: mysql:mysql-connector-java version 8.0.16
  - application.properties driver: com.mysql.jdbc.Driver (legacy class name)
- Hibernate ORM:
  - hibernate-core: 5.4.24.Final
  - hibernate-entitymanager: 4.3.10.Final (legacy; entitymanager is subsumed in core for modern Hibernate)
  - hibernate-c3p0: 4.3.10.Final
  - application.properties dialect: org.hibernate.dialect.MySQL5Dialect
- Liquibase:
  - liquibase-maven-plugin: 3.0.5 (via optional profile with-liquibase)
  - changelogs in src/main/resources/db/changelog
- ActiveMQ:
  - activemq-client: 5.10.0
  - activemq-spring: 5.10.0
  - JMS API is javax.* (geronimo-jms_1.1_spec)
- Ehcache:
  - net.sf.ehcache: 2.9.1
  - Spring integration via EhCacheCacheManager (Spring XML)
- Jetty:
  - 9.4.48.v20220622
  - Embedded startup class: src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java
  - Webapp resources under src/main/resources/webapp/WEB-INF
- Build and packaging:
  - Maven Shade Plugin 3.5.1 produces an Uber JAR with mainClass com.mycompany.entapp.snowman.EnterpriseApplication
  - Maven Resources plugin copies webapp resources into the classpath
  - No compiler/surefire/failsafe plugin blocks explicitly declared
- Key configuration files and code paths for migration:
  - application.properties: src/main/resources/application.properties
  - Spring DB context: src/main/resources/META-INF/application-context-db.xml
  - Spring Messaging context: src/main/resources/META-INF/application-context-messaging.xml
  - Spring Cache context: src/main/resources/META-INF/application-context-cache.xml
  - Servlet and web XML: src/main/resources/webapp/WEB-INF/web.xml, SpringServlet-servlet.xml, ehcache.xml
  - AbstractJDBCDao: src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/AbstractJDBCDao.java
  - DBHealthCheck: src/main/java/com/mycompany/entapp/snowman/infrastructure/db/health/DBHealthCheck.java
  - Embedded Jetty bootstrap: src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java

Notes:
- The stack is currently javax.* based. Moving to Spring 6 and Jetty 11 implies jakarta.* namespace transitions.

## 3) Action items checklist

Track progress using the checkboxes below. Leave items unchecked until completed.

### Build system
- [ ] Set Maven compiler release/source/target for Java 21 and/or configure maven-toolchain-plugin with JDK 21.
- [ ] Ensure shade packaging still produces a runnable Uber JAR under Java 21.
- [ ] Add/update plugins to Java 21–compatible versions (maven-compiler-plugin 3.11+, surefire 3.2+, failsafe 3.2+, shade 3.5+).
- [ ] Add maven-enforcer-plugin to require Java 21.
- [ ] Update Maven wrapper (mvnw/mvnw.cmd) if present to recent Maven for JDK 21 support.

### Dependencies
- [ ] Update MySQL driver to latest 8.x and switch to com.mysql.cj.jdbc.Driver in properties and any code.
- [ ] Update Hibernate ORM to a Java 21–compatible version; use unified hibernate-core (remove legacy hibernate-entitymanager).
- [ ] Update Liquibase to a modern, Java 21–compatible version; validate plugin usage.
- [ ] Update ActiveMQ client to a Java 11+–compatible version (5.17+ recommended) or evaluate Artemis; align Spring JMS config.
- [ ] Update Ehcache (consider Ehcache 3.x) and JCache API if used; align Spring Cache configuration.
- [ ] Update Jetty to a Java 21–compatible version (Jetty 11.x for Jakarta Servlet 5) if moving to Jakarta.
- [ ] Remove deprecated javax.* dependencies; migrate to jakarta.* where required by upgraded dependencies (Servlet/JPA/JMS/Validation/EL).

### Code changes
- [ ] Replace com.mysql.jdbc.Driver with com.mysql.cj.jdbc.Driver in:
      - src/main/resources/application.properties
      - src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/AbstractJDBCDao.java
- [ ] Eliminate hardcoded JDBC settings; inject Spring-managed DataSource:
      - Refactor AbstractJDBCDao and DBHealthCheck to use Spring’s DataSource/JdbcTemplate from application-context-db.xml.
- [ ] Address illegal reflective access warnings by upgrading libraries or adding proper flags if necessary.
- [ ] Replace legacy date/time usage with java.time where applicable.
- [ ] If adopting JPMS later, capture module names and add module-info only after library compatibility is verified.

### Spring compatibility
- [ ] Verify Spring Framework version for Java 21; consider upgrading to Spring 6.x if moving to Jakarta.
- [ ] If moving to Spring 6/Jakarta:
      - Update imports from javax.* to jakarta.* for Servlet/JPA/JMS/Validation/EL.
      - Update XML schemas and Spring artifacts accordingly.

### Testing and CI
- [ ] Ensure unit/integration tests run on Java 21 (update surefire/failsafe plugin versions).
- [ ] Update CI toolchain to install/use JDK 21 (build matrix and environment).
- [ ] Verify PowerMock/Mockito compatibility (consider avoiding PowerMock if possible under Java 21).

### Runtime verification
- [ ] Application starts successfully on Java 21 and listens on intended port (current default is 8090; adjust to 3001 if required by ops).
- [ ] MySQL connectivity verified; CRUD paths operate.
- [ ] Liquibase migrations execute successfully end-to-end.
- [ ] Messaging to ActiveMQ verified (send/receive on configured queues/topics).
- [ ] Caching via Ehcache verified with expected hits.

## 4) Risks and mitigations

- javax to jakarta namespace transitions:
  - Risk: Upgrading Jetty/Spring/Hibernate/Validation/EL may require code and XML import changes.
  - Mitigation: Phase upgrades; first align dependencies on Java 21 while staying javax where possible; then migrate to Jakarta stack.
- MySQL driver and dialect changes:
  - Risk: Using legacy com.mysql.jdbc.Driver and MySQL5Dialect may break or be suboptimal.
  - Mitigation: Switch to com.mysql.cj.jdbc.Driver and org.hibernate.dialect.MySQL8Dialect; test migrations and SQL compatibility.
- Illegal reflective access:
  - Risk: Older libraries (e.g., Hibernate validator, older ActiveMQ) may use reflective access.
  - Mitigation: Upgrade to versions that avoid illegal access; capture any flags needed short-term; remove flags post-upgrade.
- Shade Uber JAR and resource resolution:
  - Risk: Packaged web resources and WEB-INF might not resolve the same under Java 21 or Jetty upgrade.
  - Mitigation: Validate resource paths after plugin upgrades; adjust Resources plugin or classpath resource lookups as needed.
- PowerMock on Java 21:
  - Risk: PowerMock may not be fully compatible with newer JDKs.
  - Mitigation: Prefer Mockito and test refactors to avoid PowerMock usage where feasible.

## 5) Validation plan

Perform these validations on both local and CI builds with JDK 21:

- Build verification:
  - mvn -B -e -DskipTests=false clean verify using JDK 21
  - Ensure Maven Enforcer passes (requires 21)
- Smoke tests:
  - Start the application (shaded JAR) and hit REST endpoints:
    - Health/status endpoints (e.g., HealthCheckRestEndpoint) should return 200
    - CRUD endpoints for employee, project, user, client should respond correctly
- Database migration:
  - Run Liquibase with updated plugin or profile and verify changelogs apply without error
- Messaging:
  - Use existing adapters to send a test message to ActiveMQ; verify it is accepted or consumed
- Caching:
  - Trigger code paths that populate the cache (e.g., client cache) and confirm hits with logs

Record results in your PR descriptions and in the central tracking documents:
- kavia-docs/Java21_Migration_Progress.md (status tables)
- This tracker’s checklist updates

## 6) Rollout plan

- Branch strategy:
  - Use a long-lived feature branch: feature/java-21-migration
  - Keep PRs small and focused (build, JDBC driver, Hibernate bump, messaging, cache, etc.)
- PR review checklist (each PR):
  - Code compiles and tests pass on JDK 21
  - No unresolved illegal-access warnings or transient flags without tracking
  - Updated documentation: this tracker and Java21_Migration_Progress.md tables
  - Backward compatibility and rollback steps described
- Rollback plan:
  - Revert the specific PR
  - Update status tables back to previous state
  - Restore any removed javax.* dependencies only if necessary and documented

## 7) Ownership and timeline

Provide or update owners and due dates for each item. Use this table as the single source of truth.

| Area | Item | Owner | Due Date | Status | Notes |
|------|------|-------|----------|--------|-------|
| Build | Set compiler to 21 / add toolchain |  |  | Not Started |  |
| Build | Update plugins (compiler, surefire, failsafe, shade, enforcer) |  |  | Not Started |  |
| Dependencies | MySQL driver to latest 8.x; switch to com.mysql.cj.jdbc.Driver |  |  | Not Started | Update application.properties and AbstractJDBCDao |
| Dependencies | Hibernate to Java 21–compatible; remove hibernate-entitymanager |  |  | Not Started | Use MySQL8Dialect |
| Dependencies | Liquibase to modern version |  |  | Not Started | Validate changelogs |
| Dependencies | ActiveMQ client to 5.17+ or Artemis |  |  | Not Started | Ensure Spring JMS compat |
| Dependencies | Ehcache migration plan (2.x -> 3.x) |  |  | Not Started | Adjust Spring Cache config |
| Jetty | Upgrade to Jetty 11 (if Jakarta path) |  |  | Not Started | Align servlet APIs |
| Code | Refactor AbstractJDBCDao/DBHealthCheck to Spring DataSource |  |  | Not Started | Use JdbcTemplate/tx |
| Spring | Evaluate Spring 6/Jakarta migration |  |  | Not Started | Update XML schemas/imports |
| CI | Update CI to JDK 21 |  |  | Not Started | Matrix/build images |
| Runtime | Port verification (target 3001 if required) |  |  | Not Started | EnterpriseApplication default 8090 |

## 8) File references for targeted refactors

Use these concrete paths for the refactor and configuration steps:

- application.properties
  - Path: src/main/resources/application.properties
  - Change: jdbc.driverClassName -> com.mysql.cj.jdbc.Driver
  - Update dialect to org.hibernate.dialect.MySQL8Dialect after upgrading Hibernate
- Spring DB configuration
  - Path: src/main/resources/META-INF/application-context-db.xml
  - Leverage DataSource and JdbcTemplate beans instead of manual DriverManager in DAOs
- AbstractJDBCDao
  - Path: src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/AbstractJDBCDao.java
  - Change: Remove hard-coded credentials/DriverManager usage; inject DataSource/JdbcTemplate via Spring
- DBHealthCheck
  - Path: src/main/java/com/mycompany/entapp/snowman/infrastructure/db/health/DBHealthCheck.java
  - Change: Use Spring JdbcTemplate; remove manual connection/statement handling
- Messaging (ActiveMQ)
  - Path: src/main/resources/META-INF/application-context-messaging.xml
  - Validate ActiveMQ client upgrade compatibility
- Caching (Ehcache)
  - Path: src/main/resources/META-INF/application-context-cache.xml and src/main/resources/webapp/WEB-INF/ehcache.xml
  - Validate Ehcache upgrade path and Spring Cache configuration
- Jetty startup
  - Path: src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java
  - Validate resource resolution after plugin/Jetty upgrades; if moving to Jetty 11, update servlet artifacts and ensure jakarta servlet APIs

## 9) Changelog

- 2025-11-20: Initial creation of Migration_Tracker.md with tailored checklist, environment details, risks, validation, rollout, and ownership sections.

## 10) Links to related docs

- kavia-docs/Java_21_Migration_Overview.md
- kavia-docs/Java21_Migration_Progress.md
- kavia-docs/Migration_Implementation_Plan.md
- kavia-docs/Migration_Architecture_and_Approach.md
- kavia-docs/Migration_Dependency_Impact_Map.md

## 11) Next recommended steps

1) Build-system scaffolding:
- Add maven-compiler-plugin with release=21 and/or maven-toolchain-plugin with JDK 21.
- Add maven-enforcer-plugin to require Java 21.

2) JDBC/Driver modernization:
- Update application.properties to com.mysql.cj.jdbc.Driver and plan dialect updates.
- Plan DAO refactors to Spring-managed DataSource/JdbcTemplate.

3) Dependency upgrades (incremental):
- Hibernate to Java 21–compatible version; remove hibernate-entitymanager.
- ActiveMQ client to 5.17+.
- Liquibase plugin to recent version.
- Evaluate Ehcache 3 migration strategy and Spring integration.

4) Evaluate Spring 6 + Jetty 11 Jakarta path:
- If chosen, sequence changes to minimize downtime: update servlet/JMS/JPA/validation artifacts and imports.

5) CI and smoke tests:
- Enable JDK 21 in CI.
- Run integration smoke: REST, DB CRUD, Liquibase, JMS, cache.

6) Keep this tracker and Java21_Migration_Progress.md updated as PRs land.

