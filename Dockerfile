FROM gradle:9.2.1-jdk21 AS build

WORKDIR /workspace/app

COPY gradlew gradlew
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY src src

RUN ./gradlew build -x test --no-daemon


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
