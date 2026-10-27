#FROM maven:eclipse-temurin:21 AS BUILD
#
#WORKDIR /app
#
#COPY pom.xml .
#RUN mvn dependency:go-offline
#
#COPY src ./src
#RUN mvn package -DskipTests
#
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#
#COPY --from=build /app/targer/*.jar app.jar
#EXPOSE 5433
#
#ENTRYPOINT ["java", "-jar", "app.jar"]