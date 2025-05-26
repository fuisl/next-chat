FROM eclipse-temurin:21

WORKDIR /app

COPY server/build/libs/server.jar /app/server.jar
COPY entrypoint.sh /app/entrypoint.sh
COPY wait-for-it.sh /app/wait-for-it.sh

RUN chmod +x /app/entrypoint.sh

EXPOSE 5001

ENTRYPOINT ["/app/entrypoint.sh"]
