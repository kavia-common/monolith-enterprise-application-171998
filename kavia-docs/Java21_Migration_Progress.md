# Java 21 Migration Tracking and Audit Guide

## Purpose and Scope

This document provides a practical plan for tracking, auditing, and verifying the in-place migration of this repository to Java 21. It focuses on clear identification of what has been migrated versus what remains, using consistent source annotations, pull request workflow conventions, and an auditable central checklist. It also captures dependency/policy gates, a verification runbook, and a lightweight automation script to summarize progress across the codebase.

Quick links:
- Migration Overview: kavia-docs/Java_21_Migration_Overview.md

## Quick Checklist (At-a-Glance)

- Create and work off feature branch: feature/java-21-migration
- Use small, scoped PRs with conventional commits and [J21] tag
- Label PRs: j21-migrated, j21-partial, or j21-blocked; use milestone: Java 21 Migration
- Annotate files with J21-MIGRATED or J21-TODO markers (temporary, to be removed at the end)
- Update the central checklist table in this document for each change
- Maintain dependency/policy gates (Maven Enforcer, plugin minimums, optional forbidden APIs rules)
- Run verification smoke tests after each PR and record results
- Use tools/j21-audit.sh to audit progress by area
- On rollback, revert PR and update this document’s tables accordingly

## Branching and PR Workflow

To ensure safe, incremental migration with traceability:

- Branch: Create and use a dedicated long-lived branch named feature/java-21-migration.
- PR Scope: Prefer small, narrowly scoped PRs to reduce risk and facilitate review.
- Commit Style: Use conventional commits (e.g., feat:, fix:, refactor:) and include a [J21] tag in the commit subject for easy identification. Example: feat(rest)!: migrate controllers to Jakarta [J21]
- PR Labels:
  - j21-migrated: The scope of the PR is fully migrated to Java 21-compatible stack.
  - j21-partial: The PR migrates some parts but leaves TODOs or remaining javax/legacy usage.
  - j21-blocked: The PR targets an area but is blocked by upstream or cross-module dependencies.
- Milestone: Assign all Java 21 PRs to the “Java 21 Migration” milestone.
- Change Summary: Add a concise change summary in the PR body and reference impacted checklist rows from this document.

## Source Annotations (Temporary)

Use in-source comments to mark the current migration state of files and directories. These markers are intended to be temporary and will be removed once the migration is complete. They are designed to be easily discovered with simple grep commands.

Examples (Java or XML files):
```java
// J21-MIGRATED: 2025-11-20 PR #123 (Spring6/Jetty11)
```

```java
// J21-TODO: requires jakarta imports / API update
```

Examples (XML):
```xml
<!-- J21-MIGRATED: 2025-11-20 PR #123 (Spring6/Jetty11) -->
```

```xml
<!-- J21-TODO: requires jakarta imports / API update -->
```

Guidelines:
- Prefer placing the marker at the top of the file.
- Include date, PR number, and a brief note about the change.
- Use J21-MIGRATED when the file is fully migrated for Java 21 compatibility.
- Use J21-TODO to capture pending work, known blockers, or interim steps.
- These markers are temporary and should be removed when the overall migration completes.

## Central Checklist Table (Primary Tracker)

The following table is the canonical tracker for migration progress, pre-populated with known areas and files. Keep this table in sync as PRs merge or roll back. “Migrated?” values should be Yes, No, or Partial. “PR #” should reference the latest PR that advanced the migration for that row.

| Area | File/Path | Migrated? (Yes/No/Partial) | PR # | Notes |
|------|-----------|-----------------------------|-----:|-------|
| Build & Tooling | monolith-enterprise-application-171998/pom.xml | No |  | Enforce JDK 21; ensure plugins meet minimum versions; consider Forbidden APIs or Jakarta enforcement if applicable |
| Build & Tooling | monolith-enterprise-application-171998/.mvn/wrapper/* | No |  | Update Maven wrapper if present |
| Build & Tooling | monolith-enterprise-application-171998/mvnw | No |  | Update wrapper script if present |
| Build & Tooling | monolith-enterprise-application-171998/mvnw.cmd | No |  | Update wrapper script if present |
| Build & Tooling | monolith-enterprise-application-171998/run.sh | No |  | Ensure Java 21 runtime and JVM flags compatibility |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/application.properties | No |  | Check compatibility and remove deprecated props |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context.xml | No |  | Spring 6/Jakarta alignment |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-db.xml | No |  | DataSource/TxManager compatibility |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-cache.xml | No |  | Ehcache config compatibility |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-messaging.xml | No |  | JMS/ActiveMQ configuration |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-rest.xml | No |  | REST/Jakarta alignment |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/META-INF/application-context-scheduling.xml | No |  | Scheduler compatibility |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/webapp/WEB-INF/web.xml | No |  | Jetty 11/Jakarta servlet alignment |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/webapp/WEB-INF/SpringServlet-servlet.xml | No |  | DispatcherServlet config for Spring 6 |
| Application Configuration | monolith-enterprise-application-171998/src/main/resources/webapp/WEB-INF/ehcache.xml | No |  | Ehcache schema and settings |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java | No |  | Jetty bootstrap; ensure Jetty 11 and Jakarta servlet alignment |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/AbstractJDBCDao.java | No |  | JDBC compatibility and exceptions |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/** | No |  | DAO implementations; JPA/Hibernate/JdbcTemplate alignments |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/** | No |  | REST endpoints; move to jakarta.* annotations if applicable |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/infrastructure/cache/** | No |  | Ehcache integration alignment |
| Code (Adapters and Core) | monolith-enterprise-application-171998/src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/** | No |  | JMS/ActiveMQ jakarta.* or compatibility |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/liquibase.properties | No |  | Validate with Java 21 runtime and plugin |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/liquibase-changelog-schema.xml | No |  | Validate changelog schema |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/changelog/001_Create_Schema.xml | No |  | Validate SQL dialect compatibility |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/changelog/002_Insert_Initial_Data.xml | No |  | Validate against updated schema/driver |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/changelog/003_Insert_App_Info.xml | No |  | Validate data changes |
| Liquibase | monolith-enterprise-application-171998/src/main/resources/db/changelog/004_Insert_Dummy_Users.xml | No |  | Validate data changes |

Usage:
- For each PR, update the appropriate row’s “Migrated?” to Yes or Partial (or revert to No on rollback), add the PR number, and summarize any nuance in Notes.
- If an entire folder is migrated, evaluate whether to break the folder into representative rows or mark the folder path as Migrated with details in Notes.

## Automated Audit (Optional Script)

To help teams quickly audit migration status, use a small script that scans for J21-MIGRATED and J21-TODO markers and summarizes findings by area.

Proposed script path: tools/j21-audit.sh

Sample script snippet:
```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "== Java 21 Migration Audit =="
echo

areas=(
  "Build & Tooling|pom.xml|run.sh|.mvn/wrapper|mvnw|mvnw.cmd"
  "Application Configuration|src/main/resources/application.properties|src/main/resources/META-INF|src/main/resources/webapp/WEB-INF"
  "Code (Adapters and Core)|src/main/java"
  "Liquibase|src/main/resources/db"
)

for area in "${areas[@]}"; do
  IFS='|' read -r name paths <<< "$area"
  echo "-- $name --"
  migrated_count=$(grep -R --line-number --include='*.java' --include='*.xml' 'J21-MIGRATED' ${paths//|/ } 2>/dev/null | wc -l || true)
  todo_count=$(grep -R --line-number --include='*.java' --include='*.xml' 'J21-TODO' ${paths//|/ } 2>/dev/null | wc -l || true)
  echo "Migrated markers: $migrated_count"
  echo "TODO markers: $todo_count"
  echo
done

echo "Detailed files with markers:"
echo
grep -R --line-number --include='*.java' --include='*.xml' -E 'J21-(MIGRATED|TODO)' src/ pom.xml run.sh .mvn/ mvnw mvnw.cmd 2>/dev/null || true
```

Example output:
```
== Java 21 Migration Audit ==

-- Build & Tooling --
Migrated markers: 1
TODO markers: 2

-- Application Configuration --
Migrated markers: 0
TODO markers: 3

-- Code (Adapters and Core) --
Migrated markers: 5
TODO markers: 7

-- Liquibase --
Migrated markers: 0
TODO markers: 0

Detailed files with markers:
src/main/java/.../EnterpriseApplication.java:1:// J21-TODO: requires jakarta imports / API update
pom.xml:2:<!-- J21-MIGRATED: 2025-11-20 PR #123 (Spring6/Jetty11) -->
...
```

Notes:
- The script is optional but recommended during migration.
- Adjust include patterns and areas if new directories or modules are added.

## Dependency and Policy Gates

Enforce compatibility and avoid regressions using Maven Enforcer and minimum plugin versions that support Java 21:

- Maven Compiler Plugin: 3.11+ (set release to 21)
- Maven Surefire/Failsafe Plugins: 3.2+ (test execution compatibility)
- Maven Shade Plugin: 3.5+ (for Uber JAR)
- Maven Enforcer Plugin: 3.4+ (enforce JDK 21 and constraints)
- Optional: Forbidden APIs or custom enforcer rules to prevent javax.* imports if the Jakarta path is chosen.

Recommended Maven Enforcer examples (to be adapted in pom.xml):
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-enforcer-plugin</artifactId>
  <version>3.4.1</version>
  <executions>
    <execution>
      <id>enforce-java</id>
      <goals><goal>enforce</goal></goals>
      <configuration>
        <rules>
          <requireJavaVersion>
            <version>[21,)</version>
          </requireJavaVersion>
        </rules>
        <fail>true</fail>
      </configuration>
    </execution>
  </executions>
</plugin>
```

If moving to Jakarta:
- Consider custom checks to disallow javax.* usage once replacement is ready. This can be accomplished via build-time checks (e.g., enforcer with banned dependencies, or static analysis) and tracked via PR labels j21-partial or j21-blocked until fully replaced.

## Runbook for Verification After Each PR

After each migration PR, run minimal smoke tests and record results in the status table:

- Application starts successfully on Java 21 (local or CI)
- REST health endpoint returns HTTP 200
- CRUD via DAO works against DB (can be an embedded/integration test or local MySQL)
- Liquibase migrations apply without error
- JMS send/receive succeeds (ActiveMQ)
- Cache hits occur as expected (Ehcache)

Status table per PR:

| PR # | App Starts | REST Health 200 | CRUD via DAO | Liquibase Applies | JMS Send/Recv | Cache Hit | Notes |
|-----:|------------|-----------------|--------------|-------------------|---------------|-----------|-------|
|  |  |  |  |  |  |  |  |

Guidelines:
- Keep entries brief and focused on pass/fail with a short note.
- Link failures to follow-up issues or PRs when necessary.

## Rollback and Documentation Hygiene

- On rollback, revert the PR and update both the central checklist and the status table accordingly.
- If a rollback removes a migrated area, change “Migrated?” from Yes/Partial back to No in this document.
- In-source markers should reflect the current state. If a rollback removes a migration, remove or adjust J21-MIGRATED markers and reinstate J21-TODO markers as needed.
- Keep this document updated as the single source of truth for migration status. Remove all temporary J21 markers at the end of the migration.

## Getting Started (Adopting This Tracking Now)

1. Create the feature branch:
   - git checkout -b feature/java-21-migration
2. Set up PR labels and milestone:
   - Labels: j21-migrated, j21-partial, j21-blocked
   - Milestone: Java 21 Migration
3. Add optional audit tooling:
   - Create tools/j21-audit.sh with the sample script above; make it executable: chmod +x tools/j21-audit.sh
4. Start with Build & Tooling:
   - Update pom.xml to enforce Java 21 and minimum plugin versions
   - Update Maven wrapper, mvnw/mvnw.cmd if present
   - Validate run.sh for Java 21 usage
   - Add J21-TODO or J21-MIGRATED markers as appropriate
   - Open a small PR with [J21] in the title, apply labels/milestone, add a short summary
   - Update the central checklist table in this document and fill in PR #
5. Proceed to Application Configuration:
   - Migrate Spring configurations and servlet descriptors for Jakarta/Spring 6 as needed
   - Annotate files with J21 markers
   - Submit small PRs and update this document
6. Move to Code (Adapters and Core) by vertical slices:
   - REST layer, DAO layer, messaging adapters, cache adapters
   - Replace javax.* imports with jakarta.* where applicable
   - Update Jetty bootstrap to Jetty 11 and validate servlet compatibility
7. Validate Liquibase:
   - Ensure migrations run under Java 21 and database driver compatibility
   - Record status in the verification table and update the central checklist
8. Continue iteratively:
   - For each PR, maintain labels, update this document, run smoke tests, and capture results
   - Use tools/j21-audit.sh to monitor overall progress

## How to Know What’s Migrated vs. Not

- Source markers:
  - J21-MIGRATED indicates fully migrated files.
  - J21-TODO indicates pending work.
- Central checklist:
  - Use “Migrated?” with Yes/No/Partial for each row and reference PR #.
- PR labels:
  - j21-migrated or j21-partial applied to merged PRs clarify scope completion.
- Audit script:
  - tools/j21-audit.sh provides a summary by area and lists files with markers.

## References

- Project Overview: kavia-docs/Java_21_Migration_Overview.md
- Existing project documentation in monolith-enterprise-application-171998/kavia-docs

