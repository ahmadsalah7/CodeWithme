FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY ./compiler-engine/compiler/pom.xml ./pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY ./compiler-engine/compiler /app

RUN mvn clean package -DskipTests

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "target/compiler-0.0.1-SNAPSHOT.jar"]
