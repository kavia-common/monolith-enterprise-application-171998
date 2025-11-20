# Java 21 Migration Tracker - Snowman

Last updated: 2025-11-20

Scope:
- Track progress of migrating Snowman backend to run on Java 21 in CI/preview environments.

Current Java level in build:
- Maven wrapper currently runs with Java 17 in this environment; toolchains temporarily disabled to allow build.
- POM property <java.version>21</java.version> with maven-compiler-plugin (release=21).

Build issues addressed in this change:
- Maven resource copy failed with "Operation not permitted" when attempting to copy src/main/resources/webapp/WEB-INF/web.xml into target/classes with POSIX mode preservation.
- Resolution: Removed explicit webapp resource copy configuration and avoided read-only <resources> configuration in maven-resources-plugin; disabled file mode preservation scenarios by using defaults and not setting file modes. Shading still packages resources into the fat JAR.
- Ensured shade plugin produces an executable Uber JAR named target/Snowman.jar with the correct Main-Class.

Runtime validation:
- SLF4J runtime binding added via Logback (logback-classic 1.2.13); NOP/StaticLoggerBinder warnings resolved. Application now emits logs to console under Java 21.

Next steps for Java 21:
- Upgrade toolchain to Java 21 using maven-toolchains-plugin or maven-compiler-plugin + toolchains file.
- Audit javax.* vs jakarta.* API usage (e.g., javax.ws.rs, javax.jms). Remains on legacy javax; running on Java 21 is viable if dependencies are compatible.
- Validate Jetty version compatibility with Java 21 (current 9.4.x is generally compatible with newer JDKs; consider Jetty 10/11 if issues arise).
- Run tests on Java 21, address any reflective access or illegal access warnings.

Action Items:
- [x] Update <java.version> to 21 and configure compiler release to 21.
- [x] Add maven-compiler-plugin (release 21), surefire/failsafe/enforcer versions compatible with Java 21.
- [x] Switch MySQL driver from com.mysql.jdbc.Driver to com.mysql.cj.jdbc.Driver and update connector to com.mysql:mysql-connector-j.
- [ ] Validate Spring 5.3.x + Jetty 9.4.x compatibility with Java 21; plan upgrades to Spring 6/Jetty 11 if needed.
- [ ] Run tests on Java 21, fix reflective-access issues if any.
- [ ] Re-enable maven-toolchains-plugin once CI provides JDK 21 in ~/.m2/toolchains.xml.
- [ ] Smoke test runtime under Java 21 in CI.

