FROM gradle:7.4-jdk11 AS build
WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:11-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
