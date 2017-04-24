FROM maven:alpine

RUN apk update

RUN apk add git

CMD git clone https://github.com/testcontainers/testcontainers-java-examples/; cd testcontainers-java-examples/selenium-container; mvn clean install; cp -r target/ /reports; 