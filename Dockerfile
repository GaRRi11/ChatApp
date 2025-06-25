# First stage: Build the user module
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .

# Build only the user module and its dependencies
RUN mvn clean package -pl user -am -DskipTests

# Second stage: Run the app
FROM eclipse-temurin:17

WORKDIR /app

# Copy only the final JAR from the previous stage
COPY --from=builder /app/user/target/user-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]
