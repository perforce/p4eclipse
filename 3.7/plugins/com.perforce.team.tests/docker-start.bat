@echo off
ECHO "Running docker commands(start)" 
ECHO "Building docker images" 
docker-compose build 

ECHO "Up docker" 
docker-compose up

ECHO "Done" 