FROM maven:alpine

RUN apk update

RUN apk add git

RUN apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
    procps \
  && pip install virtualenv \
  && rm -rf /var/cache/apk/*

CMD git clone https://github.com/EduJGURJC/testcontainers-java-examples.git; cd testcontainers-java-examples/selenium-container; git clone https://github.com/novnc/noVNC.git; mvn clean install; cp -r target/ /resources/reports; 
