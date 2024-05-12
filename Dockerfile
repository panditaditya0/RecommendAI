FROM amazoncorretto:17-alpine-jdk
MAINTAINER baeldung.com
COPY target/RecommendAI.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
