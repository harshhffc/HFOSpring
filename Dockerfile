FROM openjdk:17-jdk

WORKDIR /app

COPY build/libs/HomefirstOneSpring-0.0.1-SNAPSHOT.war /app/hfo.war

# Expose the application port
EXPOSE 8080

# Set environment variables for MySQL connection
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/HomefirstOne
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=password

# Run the application
CMD ["java", "-jar", "hfo.war"]
