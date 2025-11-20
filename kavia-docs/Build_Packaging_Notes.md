# Snowman Build and Packaging Notes

This project is a Spring (non-Boot) application using embedded Jetty with an explicit main class:
- Main-Class: com.mycompany.entapp.snowman.EnterpriseApplication

Packaging configuration (in pom.xml):
- <build><finalName>Snowman</finalName></build> so the artifact name is Snowman
- maven-shade-plugin binds to the package phase and produces an executable fat JAR with the correct Main-Class.
- Web resources (src/main/resources/webapp/**) are included on the classpath at webapp/ so Jetty can resolve WEB-INF/web.xml.

Expected build output:
- Executable fat JAR at target/Snowman.jar

Build commands:
- Maven Wrapper:
  ./mvnw -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs
- System Maven:
  mvn -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs

Run command (preview uses a custom port):
- java -jar -Dserver.port=3001 target/Snowman.jar

Notes:
- The preview runner should not rely on run.sh; the packaging step alone yields the correct artifact.
- If you encounter permissions issues in target/, clean and rebuild:
  rm -rf target && ./mvnw -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs
