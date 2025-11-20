# Java 21 Migration Overview and Plan

## Context Summary

This repository contains a Spring-based monolithic backend demonstrating a Hexagonal Architecture (Ports and Adapters) with REST endpoints, messaging, caching, scheduling, and Liquibase-driven database migrations. It is packaged as an executable Uber JAR using Maven Shade with an embedded Jetty server.

Key technical context derived from the codebase:
- Build tool: Maven (pom.xml present; no Gradle files found).
- Java level today: 1.7 declared in pom.xml.
- Spring Framework: 4.x (properties specify [4.3.18,)).
- Embedded HTTP server: Jetty 9.x (javax.servlet).
- REST stack: Spring MVC (web.xml DispatcherServlet), RestTemplate configured.
- Persistence: Hibernate core 5.4.x with legacy entitymanager/c3p0 artifacts, Hibernate Validator 5.x, JPA usage via Hibernate APIs.
- Database: MySQL (mysql-connector-java 8.0.16), multiple RDBMS options for demo; Liquibase Maven plugin 3.0.5 with changelogs.
- Messaging: JMS with ActiveMQ 5.x (activemq-spring 5.10.0) and Spring JMS.
- Caching: Ehcache 2.x via Spring Cache (EhCacheCacheManager) with WEB-INF/ehcache.xml.
- Scheduling: Quartz 2.3.x.
- Packaging: Maven Shade Plugin builds Snowman.jar; main class com.mycompany.entapp.snowman.EnterpriseApplication that boots embedded Jetty from web.xml.
- Configuration: Spring XML contexts (META-INF/application-context*.xml) and webapp/WEB-INF/web.xml.
- JAX-RS: javax.ws.rs-api dependency present; controllers use Spring MVC (not Jersey), with a component-scan include filter for javax.ws.rs.Path, but actual REST endpoints are Spring @Controller/@RequestMapping classes.

Preview notes:
- The broader environment references a Spring container “preview on port 3001” and an empty .env in this workspace. In this repo, the embedded Jetty default port is 8090 and settable via -Dport. Adjust as needed during runtime packaging updates.

## Migration Strategy and Conventions

Target Java version: 21 (LTS)

Overall approach:
- Perform a non-modular migration first. Do not introduce JPMS module-info.java initially. Once stable on Jakarta EE APIs and modern dependencies, optionally define module boundaries.
- Migrate framework stack to Jakarta EE 9+ namespaces (javax.* → jakarta.*). This requires moving to Spring Framework 6.x or Spring Boot 3.x, Hibernate 6.x, and compatible stacks.
- Prefer reproducible builds and UTF-8 encoding with deterministic plugin versions.
- Avoid Java preview features by default. Keep compilation to standard features unless compelling reasons arise.
- Default to not enforcing warnings-as-errors initially; enable after stabilizing upgrades to reduce friction.

Language levels and compiler flags:
- Source/Target: 21
- Encoding: UTF-8
- Recommended Maven compiler args:
  - release: 21 (preferred) or source: 21 and target: 21
  - -parameters to retain parameter names (optional)
- Reproducible build options:
  - Maven: use maven-jar-plugin with reproducible builds; ensure plugin versions are pinned.

Runtime packaging and server options:
- Option A (keep embedded Jetty):
  - Upgrade to Jetty 11 (Jakarta Servlet 5) or Jetty 12 (Servlet 6). Adjust web.xml and programmatic configuration as needed for Jakarta namespaces and ensure Spring 6 + Spring MVC is used.
  - Continue building an Uber JAR with Maven Shade. Validate shading of Spring module metadata resources.
- Option B (migrate to Spring Boot):
  - Introduce Spring Boot 3.2.x/3.3.x with spring-boot-starter-web, spring-boot-starter-validation, spring-boot-starter-jdbc/jpa, spring-boot-starter-activemq, etc.
  - Replace embedded Jetty bootstrap code with Boot auto-config. Use Boot’s built-in packaging to produce a fat jar.
  - If Jetty (instead of Tomcat) is preferred, include spring-boot-starter-jetty and exclude Tomcat.

Modules (JPMS):
- Recommendation: skip module-info.java until the Jakarta migration is complete and dependencies are stable. Optionally add module descriptors later to encapsulate packages.

## Change Tracking Methodology

Branching workflow:
- Create a feature branch: chore/java-21-upgrade

Commit granularity plan:
1) Build config bump
   - Update Java toolchain/source/target to 21.
   - Update plugin versions and CI Java matrix.
   - Ensure UTF-8 and reproducible build config are set.

2) Dependency upgrades (grouped, but logically ordered)
   - Move Spring to Spring Framework 6.x or Spring Boot 3.2/3.3 (if adopting Boot).
   - Upgrade Jakarta stack: Servlet (Jetty), JPA, Validation, JMS.
   - Hibernate ORM 6.2/6.4, Hibernate Validator 8.x, Jackson 2.16+, SLF4J 2.x, Logback 1.4+.
   - Liquibase 4.24+, MySQL driver to mysql-connector-j 8.3+ or 9.x.
   - ActiveMQ client 5.18+ or consider Artemis 2.31+ with JMS 2.x.
   - Ehcache 3.10+ (if you plan to use JCache, move to jakarta.cache or switch to Ehcache native API).

3) Jakarta namespace migration
   - Rewrite imports: javax.* → jakarta.* for JPA, Validation, Servlet (if any servlet filters/listeners), JMS.
   - Update Spring XML and annotations as needed; Spring 6 uses Jakarta.

4) Code fixes for removed/changed APIs
   - Adjust Hibernate 6 Query types, identifier generators, configuration props, and transaction integration.
   - Update Ehcache configuration or switch to programmatic config for v3.
   - Update ActiveMQ JMS code for jakarta.jms package and any API differences.

5) Tests
   - Migrate to JUnit 5.10+, Mockito 5+. Replace deprecated PowerMock usage where possible.
   - Update Spring Test to align with Spring 6/Boot 3.

6) Runtime packaging adjustments
   - Shade plugin configuration fix if staying with embedded Jetty; or move to Spring Boot packaging.
   - Revise main class if moving to Boot; update Docker base image if used.

Change tracking tools:
- Maven:
  - Use versions-maven-plugin to generate dependency updates and lock versions.
  - Use maven-enforcer-plugin for Java 21 and dependency convergence.
  - Generate dependency trees/reports before and after for diff.
- Optional quality tracking:
  - SpotBugs/Checkstyle: run before and after; record summary diffs.

## Build Tool Adjustments

The repository uses Maven; Gradle guidance is also provided for teams that might port to Gradle.

### Maven

- Java toolchain and compiler:
  - Set maven-toolchains-plugin or JDK via CI; ensure maven-compiler-plugin uses release 21.
  - Configure UTF-8 and reproducible builds.

- Plugins:
  - maven-compiler-plugin: 3.11.0+ with release 21
  - maven-surefire-plugin: 3.1.2+ (JUnit 5 support)
  - maven-failsafe-plugin: 3.1.2+ (for ITs)
  - maven-enforcer-plugin: 3.4.1+ (require Java 21, ban duplicates, enforce dependency convergence)
  - versions-maven-plugin for managing versions
  - shade plugin: 3.5.x if remaining on shade packaging
  - liquibase-maven-plugin: 4.24+

Example Maven snippets:

- Enforcer and Compiler:
```xml
<properties>
  <maven.compiler.release>21</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <release>${maven.compiler.release}</release>
          <encoding>${project.build.sourceEncoding}</encoding>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>3.4.1</version>
        <executions>
          <execution>
            <id>enforce</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
              <rules>
                <requireJavaVersion>
                  <version>[21,)</version>
                </requireJavaVersion>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

- Liquibase:
```xml
<plugin>
  <groupId>org.liquibase</groupId>
  <artifactId>liquibase-maven-plugin</artifactId>
  <version>4.27.0</version>
  <configuration>
    <propertyFile>src/main/resources/db/liquibase.properties</propertyFile>
  </configuration>
</plugin>
```

- Shade (if staying with Uber JAR):
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.5.1</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>shade</goal></goals>
      <configuration>
        <finalName>Snowman</finalName>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>com.mycompany.entapp.snowman.EnterpriseApplication</mainClass>
          </transformer>
          <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
            <resource>META-INF/spring.handlers</resource>
          </transformer>
          <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
            <resource>META-INF/spring.schemas</resource>
          </transformer>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### Gradle

If migrating to Gradle:
- Use Gradle 8.5+ (8.7+ recommended) for Java 21 support; update wrapper.
- Configure Java toolchain to 21 and UTF-8 compile options.

Example Gradle snippets:

- settings.gradle: define version catalogs for dependency management (optional).
- build.gradle:
```groovy
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}
tasks.withType(JavaCompile).configureEach {
  options.encoding = 'UTF-8'
}
test {
  useJUnitPlatform()
}
```

- Dependency updates:
```groovy
plugins {
  id 'com.github.ben-manes.versions' version '0.51.0'
}
```

## Jakarta EE Migration Notes

Namespaces:
- JPA: javax.persistence.* → jakarta.persistence.*
- Validation: javax.validation.* → jakarta.validation.*
- Servlet: javax.servlet.* → jakarta.servlet.* (relevant for embedded Jetty/web.xml and any filters/listeners)
- JMS: javax.jms.* → jakarta.jms.*
- JAX-RS API removal if unused; if used, move to Jakarta RESTful Web Services (jakarta.ws.rs.*) and align with Spring or Jakarta stack as needed.

Framework/library alignments:
- Spring Framework 6.x or Spring Boot 3.x use Jakarta packages.
- Hibernate ORM 6.2/6.4 implements Jakarta Persistence 3.x.
- Hibernate Validator 8.x implements Jakarta Validation 3.x.
- Jetty 11 uses Jakarta Servlet 5; Jetty 12 supports Servlet 6 and EE 10 Core.
- ActiveMQ client libraries need Jakarta JMS; consider Artemis for JMS 2.x alignment.
- Ehcache 2.x is pre-Jakarta; Ehcache 3.x integrates with JCache 1.1 (javax.cache) historically; for Jakarta, either:
  - Use Ehcache 3 with its native API and avoid JCache layer, or
  - Migrate to a Jakarta cache provider when available, or leverage Spring Cache with an alternative implementation.

## Dependency Impact Assessment and Target Versions

Because this repo is currently on Java 7-era versions, the migration spans multiple major version jumps. Recommended targets known to work on Java 21:

- Spring (choose one path):
  - Spring Boot: 3.2.x or 3.3.x (Java 17+ baseline; supports Java 21). If Boot is adopted, use starters for web, validation, data-jpa, activemq, cache.
  - Spring Framework (non-Boot): 6.1.x or 6.2.x with manual configuration.

- Jetty:
  - Jetty 11.0.x for Servlet 5 (Jakarta), or Jetty 12.0.x for Servlet 6. If staying with embedded Jetty and web.xml, convert to Jakarta servlet schema and ensure Spring 6 compatibility.

- Hibernate ORM:
  - 6.2.x or 6.4.x (Jakarta Persistence 3).

- JPA / Jakarta Persistence:
  - jakarta.persistence-api 3.1.x (via Hibernate 6).

- Validation:
  - Jakarta Validation 3.x; Hibernate Validator 8.x.

- Jackson:
  - 2.16.x or newer.

- MySQL JDBC driver:
  - mysql-connector-j 8.3.x or 9.0.x; confirm Java 21 support and class name com.mysql.cj.jdbc.Driver.
  - Update application.properties driver class and dialect.

- Liquibase:
  - 4.24+ (current recommended 4.27.0 at time of writing).

- ActiveMQ:
  - ActiveMQ Classic 5.18+ with Jakarta JMS where applicable, or
  - ActiveMQ Artemis 2.31+ (JMS 2.x). Ensure Spring JMS aligns with jakarta.jms.

- Ehcache:
  - Prefer Ehcache 3.10+ for modern JDKs. Revisit Spring Cache configuration to use org.ehcache integration. If relying on JCache abstractions, plan for Jakarta cache alternatives or use Ehcache native API with Spring Cache.

- Logging:
  - SLF4J 2.0.x; Logback 1.4.x.

- Testing:
  - JUnit 5.10+, Mockito 5+, AssertJ 3.25+ (optional), Spring Test aligned to Spring 6/Boot 3.

## Code Changes Checklist

Namespaces and imports:
- javax.persistence.* → jakarta.persistence.*
- javax.validation.* → jakarta.validation.*
- javax.jms.* → jakarta.jms.*
- javax.servlet.* → jakarta.servlet.* (web.xml, any servlet/filter/listener usage)
- If any javax.ws.rs.* are in use, migrate to jakarta.ws.rs.* or rely solely on Spring MVC.

Spring configuration:
- Migrate XML contexts to Spring 6 equivalents; schema locations remain on springframework.org but ensure versions compatible with Spring 6.
- Consider moving to Java-based configuration if adopting Boot, to simplify.

Hibernate 6:
- Query API now uses jakarta.persistence.Query/TypedQuery; many APIs changed:
  - org.hibernate.query.Query usage and method signatures might differ.
  - Identifier generators configuration updated (e.g., GenerationType and strategies).
  - Configuration properties: verify hibernate.* properties for Hibernate 6 (e.g., dialect, naming strategies).
- Update dialect: for MySQL 8, use org.hibernate.dialect.MySQLDialect (modern unified) or a specific MySQL8 dialect depending on version.
- Ensure transaction manager and EntityManagerFactory beans align with Spring 6 and Hibernate 6.

JDBC and driver:
- Update driver: com.mysql.cj.jdbc.Driver
- Update JDBC URL as needed; ensure SSL and timezone params if required.

Ehcache:
- Rework from Ehcache 2.x XML to Ehcache 3.x configuration (XML or programmatic).
- Reconfigure Spring Cache: use org.springframework.cache.jcache.JCacheCacheManager (if Jakarta JCache available) or Ehcache 3 Spring integration. Alternatively, use Caffeine via Spring Cache for simplicity.

Messaging (JMS):
- Update to jakarta.jms.* API.
- Spring JMS templates and listener containers must be Spring 6 compatible.
- Update ActiveMQ client and broker dependencies accordingly.

REST and Jetty:
- If keeping embedded Jetty: migrate web.xml to Jakarta namespace and ensure the DispatcherServlet and Spring MVC work with Jakarta Servlet API.
- If adopting Boot: remove web.xml and embedded Jetty bootstrap; use Spring Boot auto-config and properties for server port.

Liquibase:
- Plugin upgrade to 4.24+; verify changelog compatibility. Update driver class reference.

XML schemas:
- Update any servlet-related XSDs to Jakarta equivalents if web.xml is still used with Jetty 11/12.

## Automated Rewrite Aids

OpenRewrite recipes can accelerate the migration:

- Add openrewrite-maven-plugin and run recipes:
  - migrate-to-java-21
  - spring-boot-3 (if adopting Boot)
  - spring-framework-6 (if non-Boot)
  - jakarta-ee-9
  - hibernate-60

Example configuration:

```xml
<plugin>
  <groupId>org.openrewrite.maven</groupId>
  <artifactId>rewrite-maven-plugin</artifactId>
  <version>5.33.0</version>
  <configuration>
    <activeRecipes>
      <recipe>org.openrewrite.java.migrate.UpgradeToJava21</recipe>
      <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2</recipe>
      <recipe>org.openrewrite.java.spring.framework.UpgradesToSpringFramework_6_1</recipe>
      <recipe>org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta</recipe>
      <recipe>org.openrewrite.hibernate.upgrade.Hibernate60</recipe>
    </activeRecipes>
  </configuration>
  <executions>
    <execution>
      <goals><goal>dryRun</goal></goals>
    </execution>
  </executions>
</plugin>
```

Run:
- mvn -U -Drewrite.recipeDryRun=true rewrite:dryRun
- mvn rewrite:run

## Testing and Verification Plan

- Unit tests:
  - Migrate to JUnit 5.10+ and Mockito 5+. Remove PowerMock usage or replace with alternative test structures if feasible (PowerMock compatibility is limited).
  - Ensure Spring Test dependencies align with Spring 6.

- Integration tests:
  - Verify Liquibase migrations apply successfully on Java 21.
  - Run against MySQL 8.0+ (containerized or local). Ensure driver and dialect updated.
  - Validate JMS send/receive using updated ActiveMQ/Artemis client with jakarta.jms.
  - Validate Ehcache behavior after moving to Ehcache 3 or alternative.

- Application smoke tests:
  - Start the application; verify REST endpoints return expected payloads.
  - Exercise CRUD endpoints and path mappings.
  - Run scheduling tasks and ensure no ClassNotFoundExceptions with Jakarta packages.

## CI/CD Updates

- Update CI matrix/tooling:
  - Build and test using Java 21 (e.g., Eclipse Temurin 21). Optionally keep a Java 17 job until migration completes.
  - Ensure surefire/failsafe test reports are collected.
  - Enable build cache where supported.

- Docker:
  - Base image: eclipse-temurin:21-jre (runtime) or eclipse-temurin:21-jdk (build stage).
  - If switching to Boot, use layered jars for efficient image builds.

## Rollout Plan and Rollback

- Use feature branch and PR review.
- Deploy to staging with database migrations applied by Liquibase; confirm rollback scripts exist for changesets where possible.
- Canary testing in production-like environment.
- Rollback strategy:
  - Revert the feature branch if required.
  - Liquibase rollbacks only if rollback scripts are authored; otherwise, plan compensating forward migrations.

## Deliverables and Change Log Tracking

Expected files to change or be added:
- pom.xml: Java 21, plugin versions, dependency upgrades, enforcer rules; possibly add OpenRewrite plugin.
- src/main/resources/application.properties: update MySQL driver class and Hibernate dialect.
- Spring XMLs in META-INF: update bean definitions and, if necessary, schema versions compatible with Spring 6. Consider migrating to Java config or Boot application.yml.
- webapp/WEB-INF/web.xml: migrate to Jakarta servlet schema or remove if using Spring Boot.
- Ehcache configuration: replace WEB-INF/ehcache.xml with Ehcache 3 configuration; update Spring Cache beans.
- JMS configuration: update ActiveMQ client versions and bean wiring for jakarta.jms.
- Code under src/main/java and tests: replace javax imports, update Hibernate usage, adjust tests for JUnit 5.
- CI configuration (e.g., GitHub Actions, Travis, Jenkinsfile): switch to Java 21.
- Dockerfile(s) if present: base image to Java 21.

Change log tracking:
- Produce mvn dependency:tree before/after and attach to PR.
- Document commit-by-commit changes in the PR description mapping to the plan’s steps. Capture breaking API adjustments explicitly.

## Maven vs Spring Boot Path Decision

- Keeping embedded Jetty:
  - Pros: Minimal change to runtime model; maintain existing Jetty bootstrap.
  - Cons: Manual management of many Jakarta and Spring 6 details; keep web.xml updated to Jakarta schema.

- Migrating to Spring Boot:
  - Pros: Simplifies dependency alignment for Jakarta and Java 21, provides auto-configuration, health endpoints, and modern packaging.
  - Cons: Requires refactoring to remove web.xml and embedded Jetty bootstrap, replacing with Boot’s structure.

Given the current state (Spring 4.x, XML config, Jetty bootstrap, Java 7), moving directly to Spring Boot 3.2/3.3 is a pragmatic route to modernize and reduce migration complexity, while retaining Jetty via spring-boot-starter-jetty if desired.

## Example Jakarta Schema Update (if keeping web.xml)

If staying with embedded Jetty and a web.xml, migrate to Jakarta Servlet schema:

```xml
<web-app version="5.0"
         xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
         metadata-complete="true">

  <servlet>
    <servlet-name>SpringServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>SpringServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>
```

## OpenRewrite Run Order

- First run Java 21 migration to handle language level and commons.
- Then run jakarta migration recipes.
- Then Spring recipes (framework or boot) to align APIs.
- Finally run hibernate 6 recipes.

## Instructions for Implementation Agent

- Detect whether to follow Boot or non-Boot path based on team decision; default to Spring Boot 3.2/3.3 for reduced risk.
- Apply maven-enforcer to require Java 21; update maven-compiler to release 21.
- Upgrade Jetty, Spring, Hibernate, Jackson, Liquibase, MySQL driver, ActiveMQ, Ehcache, SLF4J/Logback as per recommended versions.
- Replace javax.* imports across code with jakarta.*; adjust Hibernate APIs.
- Convert Ehcache 2.x XML to Ehcache 3.x configuration and update Spring Cache integration.
- Update application.properties: change JDBC driver to com.mysql.cj.jdbc.Driver and adjust Hibernate dialect.
- Update CI and Docker configurations to use Java 21 base images.
- Run full test suite and integration tests; resolve failures commit by commit.
- Produce dependency reports pre/post and summarize in PR.

## Sources

- monolith-enterprise-application-171998/pom.xml
- monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java
- monolith-enterprise-application-171998/src/main/resources/META-INF/application-context.xml
- monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-rest.xml
- monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-messaging.xml
- monolith-enterprise-application-171998/src/main/resources/webapp/WEB-INF/web.xml
- monolith-enterprise-application-171998/src/main/resources/webapp/WEB-INF/ehcache.xml
- monolith-enterprise-application-171998/src/main/resources/application.properties
