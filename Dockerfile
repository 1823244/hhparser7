FROM openjdk:17-alpine  
ARG JAR_FILE=target-ruvds/hhparser5.jar  
WORKDIR /opt/app  
COPY ${JAR_FILE} app.jar  
ENTRYPOINT ["java","-jar","app.jar"]  