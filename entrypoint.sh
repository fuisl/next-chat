#!/bin/sh
/app/wait-for-it.sh mysql:3306 --timeout=30 --strict -- echo "MySQL is up"
/app/wait-for-it.sh mongodb:27017 --timeout=30 --strict -- echo "MongoDB is up"

exec java -jar /app/server.jar
