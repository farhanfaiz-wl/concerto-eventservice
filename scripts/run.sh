#!/usr/bin/env bash

if [[ -n "${SPRING_PROFILES_ACTIVE}" ]]
then
  java -jar eventservice.jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}
else
  java -jar eventservice.jar
fi