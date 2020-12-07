FROM java:8
EXPOSE 8181
COPY target/classes/application.properties application.properties
COPY target/app-1.0-SNAPSHOT-repacked.jar am.jar
ENTRYPOINT ["java","-jar","am.jar", "--spring.config.location=file:application.properties"]