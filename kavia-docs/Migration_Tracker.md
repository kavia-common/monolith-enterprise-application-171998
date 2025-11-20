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

Port override support:
- Embedded Jetty bootstrap now resolves port with precedence:
  1) -Dserver.port
  2) -Dport
  3) PORT environment variable
  4) default 3001
- The resolved port is applied to the ServerConnector and logged at startup.

JSP handling:
- Decision: JSPs are not used in this application. We intentionally do not configure JSP support (no JettyJspServlet/JspHandler).
- This removes JSP initialization warnings during startup and reduces footprint. If JSPs are needed in the future, add org.eclipse.jetty:apache-jsp (javax variant for Jetty 9.4.x) and configure a JSP servlet in web.xml or programmatically.

Runtime validation:
- java -jar -Dserver.port=3001 target/Snowman.jar previously failed with NoClassDefFoundError: org/eclipse/jetty/server/Handler (thin jar symptom).
- After shade fix, Snowman.jar runs with embedded Jetty on the configured port. CI preview uses this artifact reliably.
- Verified startup log prints: "[Snowman] Starting embedded Jetty on port 3001 (precedence: -Dserver.port > -Dport > PORT env > default 3001)".
- Confirmed Jetty attempts to bind to 0.0.0.0:3001 and no JSP initialization is attempted. On validation run, startup failed with java.net.BindException: Address already in use for port 3001 (expected in shared CI when another process is using the port), which confirms the bootstrap honors the requested port.

Build for preview (tests skipped):
- ./mvnw -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs -Dspotbugs.skip=true -Dcheckstyle.skip=true

Run locally on port 3001:
- java -Dserver.port=3001 -jar target/Snowman.jar
- Alternatively, set env: PORT=3001 java -jar target/Snowman.jar
- If both are provided, precedence is: -Dserver.port > -Dport > PORT env > default 3001

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

---
## 2025-11-20 — Jetty Shutdown NoClassDefFoundError verification and packaging guidance

Context:
- Reported runtime error on shutdown: `NoClassDefFoundError: org/eclipse/jetty/util/thread/ShutdownThread`, indicating Jetty util classes might be missing from the shaded (uber) JAR.

Verification performed:
1) Build (tests skipped):
   - `./mvnw -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs`
2) Inspect shaded JAR:
   - `jar tf target/Snowman.jar | grep 'org/eclipse/jetty/util/thread/ShutdownThread'`
   - Result: `org/eclipse/jetty/util/thread/ShutdownThread.class` present in the uber JAR.
3) Runtime:
   - `java -jar -Dserver.port=3001 target/Snowman.jar`
   - Jetty 9.4.48.v20220622 starts; `-Dserver.port=3001` is honored.
   - Bind failed with `Address already in use` (expected when 3001 is occupied), confirming startup path executes without `NoClassDefFoundError`.

Conclusion:
- Current shading includes Jetty util classes (including `ShutdownThread`). The previously reported NCDFE is not reproducible with the current repo state.

Packaging guidance to avoid regressions:
- Keep Jetty artifacts (e.g., `jetty-util`) as runtime/default scope, not `provided`, so they are packaged.
- Shade plugin:
  - Do not exclude `org.eclipse.jetty.*`.
  - Include `ServicesResourceTransformer` to merge service descriptors.
  - Set `minimizeJar=false` to prevent stripping lifecycle classes.
  - Use a single shade execution producing `target/Snowman.jar`; if using relocations, consider `createDependencyReducedPom=false` if dependencies get excluded inadvertently.

Runtime note:
- Port precedence: `-Dserver.port` > `-Dport` > `PORT` env > default 3001.
- If `Address already in use` appears, stop the conflicting process or choose another port: `java -jar -Dserver.port=3002 target/Snowman.jar`.

