# Build from the repository root:
# docker build -f report-service/Dockerfile -t lenda-gymapp/report-service:latest .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Maven needs every module POM to load the parent reactor.
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY main-service/pom.xml main-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY integration-tests/pom.xml integration-tests/pom.xml

COPY common/src common/src
COPY report-service/src report-service/src

# Stage the executable JAR under a stable name, excluding the classes JAR.
RUN mvn -B -pl report-service -am -Dmaven.test.skip=true package \
    && cp "$(find report-service/target -maxdepth 1 -type f -name '*.jar' ! -name '*-classes.jar')" /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --home-dir /app --no-create-home app

COPY --from=build --chown=app:app /build/app.jar /app/app.jar

USER app:app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
