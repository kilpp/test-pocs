FROM openjdk:24-jdk-slim
ADD /build/libs/stress-test-poc-0.0.1-SNAPSHOT.jar /stress-test-poc.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/stress-test-poc.jar"]