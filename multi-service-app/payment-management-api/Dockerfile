FROM java:8
EXPOSE 8282
COPY target/classes/application.properties application.properties
COPY target/payment-management-api-1.0-SNAPSHOT-repacked.jar pm.jar
ENTRYPOINT ["java","-jar","pm.jar", "--spring.config.location=file:application.properties"]