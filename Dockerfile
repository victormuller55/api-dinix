FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/dinix-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7001
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
