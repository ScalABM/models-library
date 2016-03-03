FROM andrewosh/binder-base

MAINTAINER davidrpugh <david.pugh@maths.ox.ac.uk>

# Install Scala
ENV BASE_URL=http://downloads.lightbend.com \
    SCALA_VERSION=2.11.7
ENV DOWNLOAD_URL $BASE_URL/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz

RUN curl -o scala-$SCALA_VERSION.tgz $DOWNLOAD_URL && \
    tar -xf scala-$SCALA_VERSION.tgz && \
    rm scala-$SCALA_VERSION.tgz && \
    echo >> .bashrc && \
    echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> .bashrc

# Install SBT
ENV BASE_URL=http://dl.bintray.com/sbt/native-packages/sbt \
    SBT_VERSION=0.13.8
ENV DOWNLOAD_URL $BASE_URL/$SBT_VERSION/sbt-$SBT_VERSION.tgz

RUN curl -Lo sbt-$SBT_VERSION.tgz $DOWNLOAD_URL && \
    tar -xf sbt-$SBT_VERSION.tgz && \
    rm sbt-$SBT_VERSION.tgz && \
    echo >> .bashrc && \
    echo 'export PATH=~/sbt-$SBT_VERSION/bin:$PATH' >> .bashrc

# Install extra Python dependencies
RUN conda -y install seaborn
