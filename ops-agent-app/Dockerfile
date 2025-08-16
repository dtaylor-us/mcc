# syntax=docker/dockerfile:1

# ----------- Build Stage ------------
FROM maven:3.9.11-eclipse-temurin-21-noble AS build

# Copy the source code and build the application.  Use a dedicated
# workspace to avoid caching issues when files change.
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -B -DskipTests package dependency:resolve
COPY src ./src
RUN mvn -q -B -DskipTests package

# ----------- Runtime Stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage.  Use a wildcard to
# accommodate versioned artifact names.
COPY --from=build /workspace/target/*.jar app.jar

# Expose the port configured in the application.  The actual port is
# controlled via the SERVER_PORT environment variable at runtime.
EXPOSE 8080

# Provide default JVM options.  Consumers can override JAVA_OPTS to
# adjust memory or GC settings.
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Entrypoint executes the JAR with the provided JVM options.  Avoids
# using 'exec' form to allow environment variable expansion.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
