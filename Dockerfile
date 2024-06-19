FROM tomcat:9.0

WORKDIR /app
COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/hfo.war

EXPOSE 8080

CMD ["java", "-jar", "hfo.war", "catalina.sh", "run"]
