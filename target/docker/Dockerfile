FROM java:latest
MAINTAINER davidrpugh <david.pugh@maths.ox.ac.uk>
WORKDIR /opt/docker
ADD opt /opt
RUN ["chown", "-R", "daemon:daemon", "."]
USER daemon
ENTRYPOINT ["bin/example-model"]
CMD []
