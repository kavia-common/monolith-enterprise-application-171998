# Java 21 Migration Tracker - Snowman

Last updated: 2025-11-20

Scope:
- Track progress of migrating Snowman backend to run on Java 21 in CI/preview environments.

Current Java level in build:
- Maven wrapper reports Java 17 runtime in environment.
- POM property <java.version>17</java.version> — migration to 21 pending.

Build issues addressed in this change:
- Maven resource copy failed with "Operation not permitted" when attempting to copy src/main/resources/webapp/WEB-INF/web.xml into target/classes with POSIX mode preservation.
- Resolution: Removed explicit webapp resource copy configuration and avoided read-only <resources> configuration in maven-resources-plugin; disabled file mode preservation scenarios by using defaults and not setting file modes. Shading still packages resources into the fat JAR.
- Ensured shade plugin produces an executable Uber JAR named target/Snowman.jar with the correct Main-Class.

Next steps for Java 21:
- Upgrade toolchain to Java 21 using maven-toolchains-plugin or maven-compiler-plugin + toolchains file.
- Audit javax.* vs jakarta.* API usage (e.g., javax.ws.rs, javax.jms). Remains on legacy javax; running on Java 21 is viable if dependencies are compatible.
- Validate Jetty version compatibility with Java 21 (current 9.4.x is generally compatible with newer JDKs; consider Jetty 10/11 if issues arise).
- Run tests on Java 21, address any reflective access or illegal access warnings.

Action Items:
- [ ] Update <java.version> to 21 when environment supports toolchain.
- [ ] Add maven-compiler-plugin with release 21 and ensure bytecode targets 21.
- [ ] Smoke test runtime under Java 21 in CI.

