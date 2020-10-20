FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/eckig/nvidia-snatcher-j.git 

FROM maven:3.6.0-jdk-11-slim
WORKDIR /app
COPY --from=0 /app/nvidia-snatcher-j /app 
RUN mvn clean package 

FROM openjdk:15-slim-buster
WORKDIR /app
COPY --from=1 /app/target/scraper-1.0-SNAPSHOT.jar /app 
CMD ["java -jar scraper-1.0-SNAPSHOT.jar"]
