
# Multi-stage build pour optimiser la taille de l'image finale
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (cache Docker layer)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Stage de production avec une image JRE minimale
FROM eclipse-temurin:17-jre-alpine

# Informations sur l'image
LABEL maintainer="JavaCraftAcademy Team"
LABEL version="1.0.0"
LABEL description="API Gateway Service pour l'architecture microservices"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 gateway && \
    adduser -D -s /bin/sh -u 1001 -G gateway gateway

# Définir le répertoire de travail
WORKDIR /app

# Copier l'artifact depuis le stage de build
COPY --from=build /app/target/gateway-service-*.jar app.jar

# Changer le propriétaire des fichiers
RUN chown -R gateway:gateway /app

# Basculer vers l'utilisateur non-root
USER gateway

# Exposer le port de l'application
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080

# Healthcheck pour Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Point d'entrée de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Métadonnées supplémentaires
LABEL org.opencontainers.image.source="https://github.com/javacraftacademy/gateway-service"
LABEL org.opencontainers.image.documentation="https://github.com/javacraftacademy/gateway-service/README.md"
LABEL org.opencontainers.image.licenses="MIT"