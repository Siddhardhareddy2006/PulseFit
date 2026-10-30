#!/usr/bin/env bash
set -e
echo "Start each command in a separate terminal, in this order:"
echo "cd eureka-server && mvn spring-boot:run"
echo "cd auth-service && mvn spring-boot:run"
echo "cd member-service && mvn spring-boot:run"
echo "cd subscription-service && mvn spring-boot:run"
echo "cd attendance-service && mvn spring-boot:run"
echo "cd api-gateway && mvn spring-boot:run"
