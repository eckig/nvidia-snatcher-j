FROM maven:3.6.0-jdk-11-slim
# Make the installation directory
RUN mkdir /opt/nvidia-snatcher-j/

# Copy the start script to the container
COPY start.sh /start.sh

# Make executable
RUN ["chmod", "+x", "start.sh"]

# Set the start script as entrypoint
ENTRYPOINT ./start.sh
