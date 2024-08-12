FROM tomcat:9.0

WORKDIR /app
COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/hfo.war

EXPOSE 8080

RUN mkdir -p /var/www/images/document_picture/

CMD ["catalina.sh", "run"]
