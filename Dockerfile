FROM andrewosh/binder-base

MAINTAINER davidrpugh <david.pugh@maths.ox.ac.uk>

ENV SCALA_VERSION 2.11.7
ENV SBT_VERSION 0.13.8

USER root

# Install Java
RUN apt-get update && \
    apt-get install openjdk-8-jdk

# Install Scala
RUN curl -o scala-$SCALA_VERSION.tgz http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
    tar -xf scala-$SCALA_VERSION.tgz && \
    rm scala-$SCALA_VERSION.tgz && \
    echo >> .bashrc && \
    echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> .bashrc

# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt-get install sbt
