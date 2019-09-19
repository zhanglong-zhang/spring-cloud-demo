#!/usr/bin/env bash
echo '>>>>>>>>>脚本执行开始'

cd /Users/apple/Documents/spring-cloud-demo/spring-cloud-demo-eureka

echo $PWD

mvn clean package dockerfile:build

docker run -p 9100:9100 --name spring-cloud-demo-eureka -d springdemo/spring-cloud-demo-eureka

echo '>>>>>>>>>脚本执行结束'



