#!/bin/bash
delete=$1
echo "Running docker commands(stop)"

docker-compose stop &&
docker-compose down &&
docker-compose kill -s  SIGINT &&


if [ "$delete" == true ];
	then
		echo "Trying to delete images" 
		docker rmi $(docker images -f "dangling=true" -q)
fi

echo "Done" 