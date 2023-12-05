FROM openjdk:18.0.2.1-slim

LABEL org.opencontainers.image.source=https://github.com/swedenconnect/swedish-eid-idp
LABEL org.opencontainers.image.description="Sweden Connect Reference IdP"
LABEL org.opencontainers.image.licenses=Apache-2.0

ADD target/swedish-eid-idp-*.jar /swedish-eid-idp.jar

ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED -Djava.net.preferIPv4Stack=true -Dorg.apache.xml.security.ignoreLineBreaks=true"

ENTRYPOINT exec java $JAVA_OPTS -jar /swedish-eid-idp.jar

EXPOSE 8443 8444 8009
