# Migration Conventions Tracker: Snowman Backend Java 21

This tracker defines team conventions, code style, language features, and project policies to be followed throughout and after the Java 21 migration.  
This living document should be updated by maintainers as migration progresses.

---

## Code Style & Project Conventions

### Language Level/Features
- **Language Level:** Use only features supported in Java 21 or below. No preview features without explicit consensus.
- **Records & Sealed Classes:** Permitted for DTO/Value Objects if agreed.
- **Switch Expressions, Pattern Matching:** Allowed; see code review policy.
- **Nullability:** Prefer `Optional` for return values that may be missing; annotate with `@Nullable`/`@NotNull` as appropriate.
- **Var Usage:** Allowed for local variables with obvious type; avoid for APIs.

### Package/Module Structure
- **Strict Hexagonal Boundaries:** No infrastructure-to-domain dependency.
- **jakarta.*:** Adopt jakarta.* imports for all EE components.
- **One public class per file** for most cases.

### Deprecated/Disallowed Practices
- Use of `javax.*` APIs or old style beans validation is not allowed post-migration.
- Remove use of `hibernate-entitymanager` as a separate artifact.
- Avoid PowerMock; prefer Mockito/JUnit Jupiter for mocks/stubs.

### Logging
- Use SLF4J 2.x as the facade; do not use System.out/System.err for application logging.
- Parameterized logging statements preferred.

### Error Handling
- Use explicit exception types (custom exceptions per domain) and meaningful messages.
- Avoid generic `Exception` handling.

### Build & CI Conventions
- **Maven Compiler Plugin:** Always target Java 21.
- Use Maven Wrapper for reproducible builds.
- All plugin versions must be declared in the parent POM.
- No "system scope" or explicit path dependencies.

### Test & CI
- All new and refactored tests should use JUnit 5 (Jupiter).
- No deprecated JUnit runners or annotations.

---

## Nullability & Parameter Checking

| Convention                 | Mandate                                                      |
|----------------------------|--------------------------------------------------------------|
| Null checks on public APIs | Use `Objects.requireNonNull` for all public API parameters   |
| Nullability annotations    | Use `@NonNull`/`@Nullable` on exposed interfaces             |
| Validation annotations     | Use jakarta.validation or Spring validation annotations only  |

---

## API, Migration & Deprecation Policy

- **Deprecation:** All deprecated code must be annotated with `@Deprecated` + Javadoc since removal schedule.
- All removed APIs/classes should be logged in migration changelog.

---

## Rollout of New Features Policy

- New Java 21 features may be used in non-core domain classes after full migration is complete and team-wide code style review.

---

## Review & Update Checklist

- [ ] Audit imports for `javax.*` and replace.
- [ ] Audit for use of removed dependencies/plugins.
- [ ] Code review to ensure conformance before merge.

**Sources:**  
- Architecture_Snowman_Backend.md  
- pom.xml  
