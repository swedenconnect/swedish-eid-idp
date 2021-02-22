FROM openjdk:11-jre

RUN mkdir /opt/swedish-eid-idp
ADD tomcat /opt/swedish-eid-idp/tomcat/
ADD scripts/dockerStart.sh /opt/swedish-eid-idp/tomcat/bin/
ADD shibboleth /opt/swedish-eid-idp/shibboleth/

# Redirecting log directories
RUN rm -rf /opt/swedish-eid-idp/tomcat/logs && ln -s /var/log/swedish-eid-idp/ /opt/swedish-eid-idp/tomcat/logs
RUN rm -rf /opt/swedish-eid-idp/shibboleth/logs && ln -s /var/log/swedish-eid-idp/ /opt/swedish-eid-idp/shibboleth/logs
RUN chmod a+x /opt/swedish-eid-idp/tomcat/bin/*.sh

EXPOSE 8443
EXPOSE 8009

VOLUME /etc/swedish-eid-idp
VOLUME /var/log/swedish-eid-idp
CMD mkdir -p /var/log/swedish-eid-idp/ && /opt/swedish-eid-idp/tomcat/bin/dockerStart.sh swedish-eid-idp

