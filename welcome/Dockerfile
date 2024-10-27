FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY ./login/pom.xml ./pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY ./login /app

RUN mvn clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/login-0.0.1-SNAPSHOT.jar"]
