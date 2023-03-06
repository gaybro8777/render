# ======================================================================================
# Multi-stage build with final 'render-ws' stage that builds an image with a Jetty
# web server hosting render web services built from this directory.
# The different stages can be built/targeted independently and then reused to speed up
# other Docker builds that rely upon the same render code base.
#
# To build (and optionally view) a image with just the basic build environment:
#   docker build -t janelia-render:latest-build-environment --target build_environment .
#   docker run -it --entrypoint /bin/bash --rm janelia-render:latest-build-environment
#
# To build (and optionally view) a image with render source files, dependencies, and compiled artifcats:
#   docker build -t janelia-render:latest-builder --target builder .
#   docker run -it --entrypoint /bin/bash --rm janelia-render:latest-builder
#
# To build (and optionally view) a slimmed down image with just compiled artifcats in /root/render-lib:
#   docker build -t janelia-render:latest-archive --target archive .
#   docker run -it --entrypoint /bin/bash --rm janelia-render:latest-archive
#
# To build a slimmed down image with just a Jetty server hosting compiled render web services:
#   docker build -t janelia-render:latest-ws --target render-ws .
#
# To run a container with the Jetty server hosting compiled render web services:
#   docker run -it --rm janelia-render:latest-ws

# ======================================================================================
# Stage 0: build_environment
#
# Install library dependencies before actually building source.
# This caches libraries into an image layer that can be reused when only source code has changed.

FROM azul/zulu-openjdk-debian:11 as build_environment
LABEL maintainer="Forrest Collman <forrestc@alleninstitute.org>, Eric Trautman <trautmane@janelia.hhmi.org>"

RUN apt-get update && apt-get install -y maven

WORKDIR /var/www/render/
COPY pom.xml .
COPY docs/pom.xml render-app/pom.xml
COPY render-app/pom.xml render-app/pom.xml
COPY render-ws/pom.xml render-ws/pom.xml
COPY render-ws-java-client/pom.xml render-ws-java-client/pom.xml
COPY render-ws-spark-client/pom.xml render-ws-spark-client/pom.xml
COPY trakem2-scripts/pom.xml trakem2-scripts/pom.xml
COPY docs/pom.xml docs/pom.xml

# use -T 1C option to multi-thread maven, using 1 thread per available core
RUN mvn -T 1C verify clean --fail-never

# ======================================================================================
# Stage 1: builder
#
# Build the source code.

FROM build_environment as builder

COPY . /var/www/render/
RUN mvn clean

# use -T 1C option to multi-thread maven, using 1 thread per available core
RUN mvn -T 1C -Dproject.build.sourceEncoding=UTF-8 package

# ======================================================================================
# Stage 2: archive
#
# Save resulting jar and war files and remove everything else.

FROM builder as archive

RUN mkdir -p /root/render-lib && \
    mv */target/*.*ar /root/render-lib && \
    printf "\nsaved the following build artifacts:\n\n" && \
    ls -alh /root/render-lib/* && \
    printf "\nremoving everything else ...\n\n" && \
    rm -rf /var/www/render/* && \
    rm -rf /root/.m2 && \
    rm -rf /root/.embedmongo

# ======================================================================================
# Stage 3: render-ws
#
# Once web service application is built, set up jetty server and deploy application to it.

# NOTE: jetty version should be kept in sync with values in render/render-ws/pom.xml and render/render-ws/src/main/scripts/install.sh
FROM jetty:10.0.13-jre11 as render-ws

# add packages not included in base image:
#   curl and coreutils for gnu readlink
USER root
RUN apt-get update && apt-get install -y curl coreutils

## need to replace jetty 10 default slf4j-api-2.x.jar with slf4j-api-1.7.x version
## see https://github.com/eclipse/jetty.project/issues/5943#issuecomment-773334144
#WORKDIR $JETTY_HOME/lib/logging
#
## should be kept in sync with render-ws/src/main/scripts/jetty/configure_web_server.sh
#ARG SLF4J_VERSION="1.7.36"
#
#RUN echo && echo "before SLF4J fix, $PWD :" && \
#    ls -alh && \
#    rm *slf4j*.jar && \
#    curl -o "slf4j-api-$SLF4J_VERSION.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/$SLF4J_VERSION/slf4j-api-$SLF4J_VERSION.jar" && \
#    echo && echo "after SLF4J fix, $PWD :" && \
#    ls -alh && \
#    echo

WORKDIR $JETTY_BASE

COPY render-ws/src/main/scripts/jetty/ .
# NOTE: sync call added to workaround 'text file busy' error ( see https://github.com/moby/moby/issues/9547 )
RUN ls -al $JETTY_BASE/* && \
    chmod 755 ./configure_web_server.sh && \
    sync && \
    ./configure_web_server.sh

COPY --from=archive /root/render-lib/render-ws-*.war webapps/render-ws.war
COPY render-ws/src/main/scripts/docker /render-docker
RUN chown -R jetty:jetty $JETTY_BASE 

EXPOSE 8080

ENV JAVA_OPTIONS="-Xms3g -Xmx3g -server -Djava.awt.headless=true" \
    JETTY_THREADPOOL_MIN_THREADS="10" \
    JETTY_THREADPOOL_MAX_THREADS="200" \
    LOG_ACCESS_ROOT_APPENDER="STDOUT" \
    LOG_JETTY_ROOT_APPENDER="STDOUT" \
    LOG_JETTY_ROOT_LEVEL="WARN" \
    LOG_JETTY_JANELIA_LEVEL="WARN" \
    MONGO_HOST="" \
    MONGO_PORT="" \
    MONGO_USERNAME="" \
    MONGO_PASSWORD="" \
    MONGO_AUTH_DB="" \
    MONGO_CONNECTION_STRING="" \
    MONGO_CONNECTION_STRING_USES_AUTH="" \
    NDVIZHOST="" \
    NDVIZPORT="" \
    NDVIZ_URL="" \
    VIEW_CATMAID_HOST_AND_PORT="" \
    VIEW_DYNAMIC_RENDER_HOST_AND_PORT="" \
    VIEW_RENDER_STACK_OWNER="" \
    VIEW_RENDER_STACK_PROJECT="" \
    VIEW_RENDER_STACK="" \
    VIEW_MATCH_OWNER="" \
    VIEW_MATCH_COLLECTION="" \
    WEB_SERVICE_MAX_TILE_SPECS_TO_RENDER="20" \
    WEB_SERVICE_MAX_IMAGE_PROCESSOR_GB=""

USER jetty
ENTRYPOINT ["/render-docker/render-run-jetty-entrypoint.sh"]
