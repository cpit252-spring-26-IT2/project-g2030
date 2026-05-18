FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean compile -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/classes /app/target/classes

CMD ["java", "-cp", "target/classes", "sa.edu.kau.fcit.cpit252.project.ServerLauncher", "ER"]