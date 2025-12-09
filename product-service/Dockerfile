FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN apk add --no-cache maven
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app.jar"]
