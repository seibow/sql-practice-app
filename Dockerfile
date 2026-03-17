FROM tomcat:10.1-jre21-temurin-jammy

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/sqlapp.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080