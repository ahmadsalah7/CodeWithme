

# RUN --mount=type=cache,target=/root/.m2 \
#     mvn dependency:go-offline -B

# COPY ./login /app

# RUN mvn clean package -DskipTests

# RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

FROM amazoncorretto:17-alpine3.22-jdk

EXPOSE 9090

RUN apk update && \
    apk add --no-cache python3 py3-pip g++


COPY ./compiler/pom.xml ./pom.xml
COPY ./compiler/target/*.jar ./application.jar


ENTRYPOINT ["java", "-jar", "application.jar"]
