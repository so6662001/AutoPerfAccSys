# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY perf-common/pom.xml perf-common/
COPY perf-engine/pom.xml perf-engine/
COPY perf-api/pom.xml perf-api/
RUN mvn -q -B -pl perf-common,perf-engine,perf-api -am dependency:go-offline || true
COPY . .
RUN mvn -q -B -DskipTests package

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/perf-api/target/perf-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
