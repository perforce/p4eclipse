@echo off
SET delete=%1
ECHO "Running docker commands(stop)"

docker-compose stop
docker-compose down

IF %delete%==true (
	SET var= 
	FOR /F "tokens=* USEBACKQ" %%F IN (`docker images -q`) DO (
		docker rmi -f %%F 
		ECHO deleting image %%F
	)
)

ECHO Done