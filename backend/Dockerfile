# ---------- Étape 1 : build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances Maven (couche séparée)
COPY pom.xml .
RUN mvn -B --no-transfer-progress dependency:go-offline

COPY src ./src
# Les tests sont déjà exécutés par la CI GitHub Actions
RUN mvn -B --no-transfer-progress -DskipTests package

# ---------- Étape 2 : runtime ----------
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 1001 app \
    && mkdir -p /app/uploads \
    && chown -R app /app

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]