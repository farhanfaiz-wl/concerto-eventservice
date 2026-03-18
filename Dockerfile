FROM eclipse-temurin:17-jdk-jammy
# Expose required port and define log dir
EXPOSE 9090
EXPOSE 9091
VOLUME /var/log/eventservice

# create a new user and group (to not run docker as a root user)
ENV USER app-eventservice
RUN groupadd -g 999 $USER && useradd -r -u 999 -g $USER $USER

# Create required path and add jar files
RUN mkdir -p /srv/www/eventservice/current
WORKDIR /srv/www/eventservice/current
COPY ./build/libs/eventservice-*.jar /srv/www/eventservice/current/
COPY ./src/main/resources/google_creds.json /srv/www/eventservice/current/
RUN mv eventservice-*.jar eventservice.jar

COPY --chown=999 ./scripts/run.sh /srv/www/eventservice/current/run.sh

# Define all default env variables
ENV GOOGLE_APPLICATION_CREDENTIALS="google_creds.json"
ENV JMX_PORT="9009"
ENV VM_GC_LOG="-XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation \
                            -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=128M -Xloggc:gc.log "

CMD ["/bin/bash", "run.sh"]
