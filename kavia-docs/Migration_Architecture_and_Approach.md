# Migration Architecture & Strategy: Upgrading Snowman Backend to Java 21

## Overview

This document presents architectural strategies, compatibility considerations, and phased approaches for upgrading the Snowman backend to Java 21. The plan is tailored to the existing Spring-based monolith, Hexagonal architecture, and technology stack.

---

## Migration Approach

### 1. Compatibility Roadmap

- **Baseline Java:** Target Java 21 LTS directly; skip intermediate LTS versions unless critical blockers arise.
- **API Namespace Transition:** Move from `javax.*` to `jakarta.*` (affects Spring, Hibernate, JPA, Bean Validation, JMS, Servlet APIs).
- **Spring Upgrade Path:** Spring 6.x as the baseline for Java 21; all related modules/plugins must be Java 17+ compatible.
- **ORM Path:** Hibernate 6.x+ is required for Jakarta API and Java 21 support; JPA interface usage must be migrated accordingly.
- **Testing:** Transition test code to JUnit 5.x and compatible Mockito/PowerMock alternatives.

### 2. Modularization & Hexagonal Discipline

- **Ports & Adapters:** Enforce separation of application, infrastructure, and domain layers. Upgrade dependencies in Adapters first, followed by Ports and Domain.
- **Encapsulation:** Refactor direct, hardwired implementations using modern Spring DI and Configuration classes.

### 3. Preview & Build Changes

- **POM Adjustments:**
  - Update `maven-compiler-plugin` to set `<release>21</release>` or set `<source>21</source>` and `<target>21</target>`.
  - Specify toolchains if CI or team workstations may default to another JDK.
  - Example:
    ```xml
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.11.0</version>
      <configuration>
        <release>21</release>
      </configuration>
    </plugin>
    ```
  - Remove or upgrade all plugins using deprecated features.

- **CI Pipeline:** Ensure the Java version in CI/CD runners is set to 21.  
  - For GitHub Actions:
    ```
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
    ```

### 4. Performance, GC, and Runtime Considerations

- Take advantage of Java 21 G1/Parallel GC improvements for latency.
- Evaluate usage of Virtual Threads (Project Loom) for REST/JMS handlers—possible future optimization.
- Profile memory and threads post-upgrade to catch regressions.

### 5. Modern Java Language Features (Optional Enablement)

- Consider refactoring select classes to use records, sealed classes, pattern matching, etc.
- Gradually introduce new language features via explicit conventions (tracked in conventions doc).

---

## Risks & Special Considerations

| Area                | Risk/Note                                                     | Mitigation                          |
|---------------------|--------------------------------------------------------------|-------------------------------------|
| CI/CD               | Incompatible build runners/JDK mismatch                      | Pin runner to Java 21               |
| Third-party plugins | Outdated plugin versions incompatible with Java 21            | Use upgrade commands, pin to latest |
| Testing             | PowerMock/legacy Mockito may break                           | Refactor for JUnit 5/modern Mockito |
| Database migration  | Liquibase versions pre-4.x fail on Java 17+                  | Upgrade to latest 4.x, test scripts |
| Jakarta Transition  | Compilation/runtime errors due to javax/jakarta mismatch     | Refactor imports, update dependencies|
| Ehcache             | v2-to-v3 config migration is nontrivial                      | Use conversion guides, plan extra test|

---

## Architectural Migration Flow (Phases)

```mermaid
flowchart TD
    A[Backup & Branch] --> B[Upgrade Build Tooling & JDK]
    B --> C[Migrate Spring + Related Frameworks to Java 17+ Compatible Versions]
    C --> D[Update All Application Dependencies/Libs]
    D --> E[Update Code: javax.* → jakarta.*; Language Features]
    E --> F[Refactor Testing Stack (JUnit 5, Mockito)]
    F --> G[Update/Refactor Ehcache, ActiveMQ, Jetty config]
    G --> H[Regression & Performance Testing]
    H --> I[CI/CD & Deployment Update]
    I --> J[Release & Monitor]
```

---

## Adoption Strategies

- Snowman remains a monolith but paves path for future modularization via strong package boundaries and Hexagonal discipline.
- Start with "build-only" compatibility (no usage of new APIs), progress to incrementally modernizing code.
- Consider a feature-flagged approach for major API changes.
- Document deprecations and behavior changes throughout for reference.

**Sources:**  
- pom.xml  
- web.xml  
- Architecture_Snowman_Backend.md  
