FROM andrewosh/binder-base

MAINTAINER davidrpugh <david.pugh@maths.ox.ac.uk>

USER root

# Install Java
ENV JAVA_VERSION 7

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-$JAVA_VERSION-jre-headless && \
    apt-get install -y --no-install-recommends openjdk-$JAVA_VERSION-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Scala
ENV SCALA_VERSION 2.11.7

RUN curl -o scala-$SCALA_VERSION.tgz http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
    tar -xf scala-$SCALA_VERSION.tgz && \
    rm scala-$SCALA_VERSION.tgz && \
    echo >> .bashrc && \
    echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> .bashrc

# Install SBT
ENV SBT_VERSION 0.13.8

RUN curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb

# Add non-Anaconda Python dependencies
RUN conda update conda && \
    conda install -y seaborn

USER main
