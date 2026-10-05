# Build from the repository root:
# docker build -f main-service/Dockerfile -t lenda-gymapp/main-service:latest .
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
COPY main-service/src main-service/src

# Stage the executable JAR under a stable name, excluding the classes JAR.
RUN mvn -B -pl main-service -am -Dmaven.test.skip=true package \
    && cp "$(find main-service/target -maxdepth 1 -type f -name '*.jar' ! -name '*-classes.jar')" /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --home-dir /app --no-create-home app

COPY --from=build --chown=app:app /build/app.jar /app/app.jar

USER app:app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
