#!/bin/bash

cd /opt/nvidia-snatcher-j/

# Download/Update
git clone https://github.com/eckig/nvidia-snatcher-j.git .
git pull --all

# Build
mvn -f pom.xml clean package
java -jar target/scraper-1.0-SNAPSHOT.jar
