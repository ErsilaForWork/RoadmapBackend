#Builder Stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /opt/app

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -Dproject.build.finalName=backend-app package -DskipTests



#Runtime
FROM eclipse-temurin:21-jre
WORKDIR /opt/app

USER root

RUN apt-get update \
 && apt-get install -y --no-install-recommends bash \
 && rm -rf /var/lib/apt/lists/*

RUN groupadd -r app && useradd -r -g app app

COPY --from=builder --chown=app:app /opt/app/target/backend-app.jar /opt/app/backend-app.jar
COPY --chown=app:app wait-for.sh /opt/app/wait-for.sh

RUN chmod +x /opt/app/wait-for.sh \
 && chown -R app:app /opt/app

USER app

EXPOSE 8080

ENTRYPOINT ["/opt/app/wait-for.sh", "roadmap_db:5432", "--timeout", "120", "--", "java", "-jar", "/opt/app/backend-app.jar"]