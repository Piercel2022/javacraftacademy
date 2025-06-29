#!/bin/bash

# Base directory
BASE_DIR="gateway-service"

# Liste des répertoires à créer
DIRS=(
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/config"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/filter"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/service"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/controller"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/dto"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/exception"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/util"
  "$BASE_DIR/src/main/resources"
  "$BASE_DIR/src/test/java/com/javacraftacademy/gateway/filter"
  "$BASE_DIR/src/test/java/com/javacraftacademy/gateway/service"
)

# Fichiers à créer
FILES=(
  "$BASE_DIR/pom.xml"
  "$BASE_DIR/Dockerfile"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/GatewayServiceApplication.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/config/GatewayConfig.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/config/SecurityConfig.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/config/CorsConfig.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/config/RedisConfig.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/filter/AuthenticationFilter.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/filter/RateLimitingFilter.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/filter/LoggingFilter.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/filter/RequestValidationFilter.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/service/AuthenticationService.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/service/JwtService.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/service/UserValidationService.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/controller/HealthController.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/controller/GatewayController.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/dto/LoginRequest.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/dto/AuthResponse.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/dto/UserInfo.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/exception/GatewayException.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/exception/AuthenticationException.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/exception/GlobalExceptionHandler.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/util/JwtUtil.java"
  "$BASE_DIR/src/main/java/com/javacraftacademy/gateway/util/ResponseUtil.java"
  "$BASE_DIR/src/main/resources/application.yml"
  "$BASE_DIR/src/main/resources/application-dev.yml"
  "$BASE_DIR/src/main/resources/application-prod.yml"
  "$BASE_DIR/src/main/resources/bootstrap.yml"
  "$BASE_DIR/src/test/java/com/javacraftacademy/gateway/GatewayServiceApplicationTests.java"
  "$BASE_DIR/src/test/java/com/javacraftacademy/gateway/filter/AuthenticationFilterTest.java"
  "$BASE_DIR/src/test/java/com/javacraftacademy/gateway/service/JwtServiceTest.java"
)

# Création des dossiers
for dir in "${DIRS[@]}"; do
  mkdir -p "$dir"
done

# Création des fichiers
for file in "${FILES[@]}"; do
  touch "$file"
done

echo "✅ Structure du projet 'gateway-service' créée avec succès."
