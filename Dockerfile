# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Resolve dependencies
RUN ./mvnw dependency:go-offline

COPY src ./src
COPY libs ./libs
RUN ./mvnw package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria um grupo e usuário de sistema não-root
RUN addgroup -S shipfast && adduser -S shipfast -G shipfast -u 10001

# Copia a aplicação construída na fase de build
COPY --from=build --chown=shipfast:shipfast /app/target/ship-fast-0.0.1-SNAPSHOT.jar app.jar

# Define o usuário não-root para execução
USER shipfast

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
