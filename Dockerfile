# VG Stolen from Confluent (comes with Apache license, so I felt entitled)

ARG DOCKER_UPSTREAM_REGISTRY
ARG DOCKER_UPSTREAM_TAG=latest

FROM ${DOCKER_UPSTREAM_REGISTRY}confluentinc/cp-base:${DOCKER_UPSTREAM_TAG}

ARG STREAMS_VERSION
ARG ARTIFACT_ID

WORKDIR /build
ENV COMPONENT="${ARTIFACT_ID}"

# We run the Kafka Streams demo application as a non-priviledged user.
ENV STREAMS_USER="streams"
ENV STREAMS_GROUP=$STREAMS_USER

ENV STREAMS_EXAMPLES_BRANCH="${CONFLUENT_MAJOR_VERSION}.${CONFLUENT_MINOR_VERSION}.x"
ENV STREAMS_EXAMPLES_FATJAR="kafka-streams-examples-${STREAMS_VERSION}-standalone.jar"
ENV STREAMS_APP_DIRECTORY="/usr/share/java/kafka-streams-examples"
ENV STREAMS_EXAMPLES_FATJAR_DEPLOYED="$STREAMS_APP_DIRECTORY/$STREAMS_EXAMPLES_FATJAR"
ENV KAFKA_MUSIC_APP_CLASS="io.confluent.examples.streams.interactivequeries.kafkamusic.KafkaMusicExample"
ENV KAFKA_MUSIC_APP_REST_HOST=localhost
ENV KAFKA_MUSIC_APP_REST_PORT=7070

ENV HOST_IP=localhost

EXPOSE $KAFKA_MUSIC_APP_REST_PORT

# This affects how strings in Java class files are interpreted.  We want UTF-8, and this is the only locale in the
# base image that supports it
ENV LANG="C.UTF-8"

ADD target/${ARTIFACT_ID}-${STREAMS_VERSION}-standalone.jar /usr/share/java/${ARTIFACT_ID}/${ARTIFACT_ID}-${STREAMS_VERSION}-standalone.jar
ADD target/${ARTIFACT_ID}-${STREAMS_VERSION}-package/share/doc/* /usr/share/doc/${ARTIFACT_ID}/

COPY include/etc/confluent/docker /etc/confluent/docker

RUN groupadd $STREAMS_GROUP && useradd -r -g $STREAMS_GROUP $STREAMS_USER

RUN mkdir /etc/$COMPONENT \
    && chown $STREAMS_USER:$STREAMS_GROUP /etc/$COMPONENT

USER $STREAMS_USER

CMD ["/etc/confluent/docker/run"]
