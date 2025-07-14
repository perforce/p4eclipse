#!/bin/bash
echo "Running docker commands(start)" 
echo "Building docker images" 
docker-compose build && 

echo "Up docker" 
docker-compose up &&


echo "Done" 