#!/usr/bin/env bash

gradle clean build
docker build -t eventservice:latest .