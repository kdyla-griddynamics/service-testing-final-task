FROM java:8
EXPOSE 8080
COPY target/classes/application.properties application.properties
COPY target/user-management-api-1.0-SNAPSHOT-repacked.jar um.jar
ENTRYPOINT ["java","-jar","um.jar", "--spring.config.location=file:application.properties"]