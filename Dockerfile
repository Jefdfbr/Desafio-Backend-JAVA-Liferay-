FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache bash curl fontconfig ttf-dejavu

WORKDIR /opt/liferay

COPY liferay /opt/liferay

RUN chmod +x /opt/liferay/tomcat/bin/*.sh

EXPOSE 8080

CMD ["/opt/liferay/tomcat/bin/catalina.sh", "run"]
