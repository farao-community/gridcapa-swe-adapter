FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=gridcapa-swe-adapter-app/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
