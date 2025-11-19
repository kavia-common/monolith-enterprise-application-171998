# Java 21 Migration Implementation Plan for Snowman Backend

A multi-phase, risk-managed approach for upgrading to Java 21, with tasks, acceptance criteria, rollback strategies, and checklists.

---

## Phase Overview

1. **Initiation & Analysis**
2. **Toolchain & Build Preparation**
3. **Core Dependency Upgrades**
4. **Jakarta & API Transition**
5. **Test & Refactor Application Code**
6. **Pre-release QA & Regression**
7. **Deployment, Monitoring & Rollback**

---

## Detailed Phase Tasks

### 1. Initiation & Analysis

**Tasks:**
- Backup repo and DB
- Create a long-lived feature branch for migration
- Run:
    ```sh
    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    ```
- Inventory of all uses of `javax.*` in sources and config

**Acceptance Criteria:**  
Migration branch exists, dependencies reports attached, initial import usage analyzed.

---

### 2. Toolchain & Build Preparation

**Tasks:**
- Install/open JDK 21 on dev and CI systems.
- Pin Maven version >=3.9.
- Upgrade maven-compiler-plugin to 3.11+, set `<release>21</release>` in POM.
- CI/CD pipeline step for Java 21 toolchain.
- Test builds and smoke run of current codebase on Java 21 (should fail, but identify blockers).

**Acceptance Criteria:**  
Codebase fails only on version/API issues, not build environment.

---

### 3. Core Dependency Upgrades

**Tasks:**
- Update all dependencies in POM to target versions (see Dependency Impact Map)
- Remove `hibernate-entitymanager`, upgrade to `hibernate-core:6.3+`.
- Upgrade Spring to 6.x, Jetty to 12.x, Ehcache to 3.10.x, ActiveMQ Spring to 5.18.x+, and JUnit/Mockito to latest
- Update Liquibase-maven-plugin to 4.x.

**Acceptance Criteria:**  
Project builds; all dependency upgrades committed; no use of disallowed/deprecated APIs/libraries.

**Rollback Plan:**  
Revert commit to feature branch and revert to Java 1.7 baseline for build.

---

### 4. Jakarta & API Transition

**Tasks:**
- Refactor all Java sources to replace `javax.*` imports with `jakarta.*`.
- Replace/upgrade Servlet, Bean Validation, JMS, JPA usage accordingly.
- Migrate config XMLs (web.xml, ehcache.xml) for compatibility, or replace with Java config as needed.
- Document all code and config changes.

**Acceptance Criteria:**  
All imports updated. No legacy `javax.*` in code or configs. CI builds pass dependency-check and static analysis.

---

### 5. Test & Refactor Application Code

**Tasks:**
- Migrate tests to JUnit 5 and modern Mockito.
- Remove PowerMock usages or provide suitable test doubles.
- Add/extend integration & regression test coverage for REST, JMS, DB.

**Acceptance Criteria:**  
`mvn clean verify` and regression tests pass.

---

### 6. Pre-release QA & Regression

**Tasks:**
- Code review for standards conformance (see Conventions Tracker).
- Performance/latency profiling under Java 21.
- Full regression and smoke testing.
- Documentation: Update README, system diagrams if any modules changed.

**Acceptance Criteria:**  
All sign-offs achieved, QA checklist passed.

**Rollback Plan:**  
Roll back to pre-migration release, redeploy previous artifact.

---

### 7. Deployment, Monitoring & Rollback

**Tasks:**
- Deploy Java 21 build artifact to staging, then production.
- Monitor logs, performance, and health during first week.
- Prepared playbook for fast rollback (redeploy old Snowman.jar, rollforward/rollback DB if schema changed).

**Acceptance Criteria:**  
Stable post-migration operation for defined burn-in period.

---

## CI Pipeline / Build Snippet Example

Update Maven compiler plugin in `pom.xml`:
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

**CI:**
```yaml
- name: Use Java 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
```

---

## Risk & Rollback

- Always back up DB before any migration.
- Never deploy without full regression.
- Prepare for rapid rollback to previous artifact and old DB schema.

---

## Appendix: Command Checklist

| Phase          | Commands/Checklist                                                      |
|----------------|------------------------------------------------------------------------|
| Analysis       | `mvn versions:display-plugin-updates`, `mvn versions:display-dependency-updates` |
| Tool Upgrade   | Java 21 install, Maven upgrade, update POM                             |
| Dependencies   | Update all plugin/deps, remove deprecated artifacts                     |
| Jakarta Trans  | `grep -Rn javax .` then refactor to jakarta                            |
| Refactoring    | `mvn clean compile`, update tests                                      |
| CI/CD Update   | Update build scripts/pipelines to Java 21                              |

**Sources:**  
- pom.xml  
- web.xml  
- ehcache.xml  
- application.properties  
- test/  
- Architecture_Snowman_Backend.md  
