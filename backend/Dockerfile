FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# copy pom trước để cache
COPY pom.xml .
RUN mvn dependency:go-offline

# copy source
COPY src ./src

# build
RUN mvn clean package -DskipTests

# chạy app
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]