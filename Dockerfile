FROM openjdk:17-jdk

WORKDIR /app
COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /app/hfo.war

# Copy the logback configuration file
COPY src/main/resources/logback-spring.xml /app/logback-spring.xml

EXPOSE 8447
CMD ["java", "-Dlogging.config=/app/logback-spring.xml", "-jar", "hfo.war"]
