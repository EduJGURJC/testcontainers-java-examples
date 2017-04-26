FROM maven:alpine

RUN apk update

RUN apk add git

RUN apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
  && pip install virtualenv \
  && rm -rf /var/cache/apk/*

CMD git clone https://github.com/EduJGURJC/testcontainers-java-examples.git; cd testcontainers-java-examples/selenium-container; cat src/test/java/SeleniumContainerTest.java
