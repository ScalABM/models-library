FROM andrewosh/binder-base

MAINTAINER davidrpugh <david.pugh@maths.ox.ac.uk>

# Install Java
ENV JAVA_VERSION=8 \
    JAVA_UPDATE=74 \
    JAVA_BUILD=02 \
    BASE_URL=http://download.oracle.com/otn-pub/java/jdk
ENV JAVA_SOURCE jdk-${JAVA_VERSION}u${JAVA_UPDATE}-linux-x64.tar.gz
ENV DOWNLOAD_URL $BASE_URL/${JAVA_VERSION}u${JAVA_UPDATE}-b${JAVA_BUILD}/$JAVA_SOURCE

# this is not secure...perhaps need to run as root?
RUN curl --insecure --location \
         --cookie "oraclelicense=accept-securebackup-cookie" \
         --output $JAVA_SOURCE \
         $DOWNLOAD_URL && \
    mkdir $HOME/java && \
    tar -zxf $JAVA_SOURCE -C $HOME/java/ && \
    rm $JAVA_SOURCE

ENV JAVA_HOME=$HOME/java/jdk1.$JAVA_VERSION.0_$JAVA_UPDATE
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Scala
ENV BASE_URL=http://downloads.lightbend.com \
    SCALA_VERSION=2.11.7
ENV DOWNLOAD_URL $BASE_URL/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz

RUN curl -o scala-$SCALA_VERSION.tgz $DOWNLOAD_URL && \
    mkdir $HOME/scala && \
    tar -xf scala-$SCALA_VERSION.tgz -C $HOME/scala/ && \
    rm scala-$SCALA_VERSION.tgz
ENV PATH $HOME/scala/scala-$SCALA_VERSION/bin:$PATH

# Install Scala Build Tool (SBT)
ENV BASE_URL=http://dl.bintray.com/sbt/native-packages/sbt \
    SBT_VERSION=0.13.8
ENV DOWNLOAD_URL $BASE_URL/$SBT_VERSION/sbt-$SBT_VERSION.tgz

RUN curl -Lo sbt-$SBT_VERSION.tgz $DOWNLOAD_URL && \
    mkdir $HOME/sbt && \
    tar -xf sbt-$SBT_VERSION.tgz && \
    rm sbt-$SBT_VERSION.tgz
ENV PATH $HOME/sbt/bin:$PATH

# Install extra Python dependencies
RUN conda install -y seaborn
