# Multi-stage build → slim runtime. The build stage pins the toolchain, so the host needs only Docker
# (not a local JDK / Maven). → ./Dockerfile (springboot.md §2)
# Gradle: swap the build image for `gradle:<ver>-jdk<JDK>` and the wrapper call for `./gradlew build`.
# Pin <JDK> to the project's toolchain (the .sdkmanrc / build-file Java version — default 25).

# --- build stage ---
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /src
COPY . .
RUN ./mvnw -B -ntp -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
