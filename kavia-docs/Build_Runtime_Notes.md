# Build and Runtime Notes

- Requires Java 17+ runtime; Java 21 migration is in-progress. The fat jar runs on Java 17 in the CI image.
- Default Jetty bind: 0.0.0.0:3001. You can override with -Dserver.port=<port> and -Dserver.address=<host>.
- Command: java -jar -Dserver.address=0.0.0.0 -Dserver.port=3001 target/Snowman.jar

- To build the application while skipping all tests (unit, integration, and test-compile), run:
  ./mvnw -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs

- Artifacts produced under target/:
  - Snowman.jar (primary, executable jar)
  - enterprise-application-1.0-SNAPSHOT-shaded.jar (fat/uber jar produced by shade plugin)

- Start the service on port 3001:
  ./run.sh
  or explicitly:
  java -jar -Dserver.port=3001 target/Snowman.jar

- If Snowman.jar is missing but a shaded jar exists, the run script will automatically fall back to the shaded jar.

- If no runnable jar exists, rebuild using the command above.
