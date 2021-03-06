FROM alpine/git as clone
WORKDIR /app
RUN git clone https://github.com/eckig/nvidia-snatcher-j.git . 

FROM maven:3-openjdk-15 as build
WORKDIR /app
COPY --from=clone /app /app 
RUN mvn clean package 

FROM openjdk:15-slim-buster
WORKDIR /app
COPY --from=build /app/target/scraper-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar
CMD java -cp app.jar main.JNvidiaSnatcher
