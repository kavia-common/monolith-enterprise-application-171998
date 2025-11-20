# Java 21 Migration Tracker - Snowman

Last updated: 2025-11-20

Scope:
- Track progress of migrating Snowman backend to run on Java 21 in CI/preview environments.

Current Java level in build:
- Maven wrapper currently runs with Java 17 in this environment; toolchains temporarily disabled to allow build.
- POM property <java.version>21</java.version> with maven-compiler-plugin (release=21).

Build issues addressed in this change:
- Observed intermittent preview failures due to missing target/Snowman.jar. After a clean build, two artifacts existed:
  - target/Snowman.jar (thin jar with only classes/resources; not executable due to missing dependencies)
  - target/enterprise-application-1.0-SNAPSHOT-shaded.jar (corrupt/undesired extra shaded jar)
- Root cause: Conflicting or legacy shade configuration attaching an additional shaded artifact while the Snowman.jar produced by maven-jar-plugin was not a fat jar. Jetty dependencies were not on the classpath at runtime for Snowman.jar.

Resolutions implemented:
- Enforced a single shade execution with finalName=Snowman and shadedArtifactAttached=false so only target/Snowman.jar is produced.
- Verified MANIFEST Main-Class = com.mycompany.entapp.snowman.EnterpriseApplication and shade includes org.eclipse.jetty.* and other runtime deps.
- Kept Jetty dependencies with compile scope (not provided) so they are packaged into the fat jar.
- Simplified resources plugin configuration to defaults to avoid permission issues.

Runtime validation:
- java -jar -Dserver.port=3001 target/Snowman.jar previously failed with NoClassDefFoundError: org/eclipse/jetty/server/Handler (thin jar symptom).
- After shade fix, Snowman.jar is expected to run with embedded Jetty on the configured port. CI preview should now use this artifact reliably.

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

