# Snowman Build and Packaging Notes

This project is a Spring (non-Boot) application using embedded Jetty with an explicit `main` class:
- Main-Class: `com.mycompany.entapp.snowman.EnterpriseApplication`

Packaging configuration (already present in `pom.xml`):
- `<build><finalName>Snowman</finalName></build>` so the artifact name is `Snowman`
- `maven-shade-plugin` binds to the `package` phase and produces an executable fat JAR with the correct `Main-Class`.
- Resources plugin includes `src/main/resources/webapp/**` on the classpath at `webapp/` so Jetty can resolve `WEB-INF/web.xml`.

Expected build output:
- Executable fat JAR at `target/Snowman.jar` (enforced via <finalName>Snowman</finalName> and shade plugin finalName).

Build commands:
- With Maven Wrapper:
  `./mvnw -q -DskipTests package`
- With system Maven:
  `mvn -q -DskipTests package`

If unit tests in this environment fail to compile (e.g., legacy Mockito imports), force packaging by skipping test compilation entirely:
- `./mvnw -q -DskipTests=true -DskipITs -Dmaven.test.skip=true package`
This will still produce the executable fat JAR at `target/Snowman.jar`.

Run command (as used by `run.sh`):
- `./run.sh 3001` (lists target/, builds if missing, and starts the discovered jar; prefers target/Snowman.jar)
- Direct Java (if you already have the jar): `java -Dport=3001 -Dserver.port=3001 -jar target/Snowman.jar`

Notes on environment error:
- If you encounter an error like:
  `FileSystemException: .../target/classes/webapp/WEB-INF/web.xml: Operation not permitted`
  this is an environment/permissions issue during the resources copy phase. Ensure the working directory and `target/` tree are writable by the current user (avoid mixed root-owned files). Clean the target directory and re-run:
  - `rm -rf target`
  - `./mvnw -DskipTests package`

No changes were necessary in `pom.xml` or `run.sh` for packaging. The configuration already produces `target/Snowman.jar` with the correct `Main-Class`.
