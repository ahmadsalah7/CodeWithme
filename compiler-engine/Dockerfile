
# RUN apt-get update && \
#     apt-get install -y apt-transport-https ca-certificates curl gnupg2 software-properties-common && \
#     curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
#     echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list && \
#     apt-get update && \
#     apt-get install -y docker-ce-cli && \
#     rm -rf /var/lib/apt/lists/*

# RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# COPY ./compiler/pom.xml ./pom.xml
# RUN --mount=type=cache,target=/root/.m2 \
#     mvn dependency:go-offline -B
# RUN mvn clean package -DskipTests

# FROM openjdk:17-jdk-slim


# RUN apt-get update && apt-get install -y \
#     python3 \
#     python3-pip \
#     python-is-python3 \
#     # docker.io \
#     g++ \
#     default-jdk \
#     && rm -rf /var/lib/apt/lists/*


FROM amazoncorretto:17-alpine3.22-jdk

EXPOSE 9090

RUN apk update && \
    apk add --no-cache python3 py3-pip g++


COPY ./compiler/pom.xml ./pom.xml
COPY ./compiler/target/*.jar ./application.jar


ENTRYPOINT ["java", "-jar", "application.jar"]
