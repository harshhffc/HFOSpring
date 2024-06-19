FROM openjdk:17-jdk
FROM tomcat:9.0

WORKDIR /app
COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/hfo.war

EXPOSE 8447

CMD ["java", "-jar", "hfo.war", "catalina.sh", "run"]
