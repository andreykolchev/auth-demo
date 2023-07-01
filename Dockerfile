FROM openjdk:17

VOLUME /tmp
VOLUME conf:/opt/conf
EXPOSE 8080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-XX:+ShowCodeDetailsInExceptionMessages"

ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar