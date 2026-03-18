#!/usr/bin/env bash

echo "Stopping and removing clientapiservice"
docker stop eventservice 2> /dev/null || true
docker rm eventservice 2> /dev/null || true

echo "Running eventservice now"
docker run \
    --name eventservice \
    --publish 9090:9090 \
    --publish 9091:9091 \
    --restart unless-stopped \
    --env SPRING_PROFILES_ACTIVE=jenkins \
    --env REDIS_HOST=127.0.0.1 \
    --env REDIS_PORT=6379 \
    --env FALCON_URI=http://52.86.16.23:8090 \
    --env FALCON_APP_NAME=dgi-app \
    --env FALCON_APP_KEY=vq345f235g4qer \
    --env ANALYTICS_URI=http://52.86.16.23:3120 \
    --env ANALYTICS_APP_KEY=analytics \
    --detach \
    eventservice:latest