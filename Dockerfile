FROM openjdk:17-jdk

WORKDIR /app
COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /app/hfo.war
EXPOSE 8080
CMD ["java","-war","hfo.war"]