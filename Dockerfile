FROM gradle:7.0.2-jdk16 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle src src
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle build.gradle build.gradle
COPY --chown=gradle:gradle settings.gradle settings.gradle

RUN gradle build --no-daemon 

FROM openjdk:16.0.1-jdk-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java","-jar","/app/spring-boot-application.jar"]
