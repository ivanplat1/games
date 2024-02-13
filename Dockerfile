FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
COPY frontend ./frontend
COPY package-lock.json ./
COPY package.json ./
CMD ["./mvnw", "spring-boot:run"]
EXPOSE 8080