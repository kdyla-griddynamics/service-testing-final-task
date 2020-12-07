FROM java:8
EXPOSE 8989
COPY target/classes/application.properties application.properties
COPY target/payment-gateway-stub-1.0-SNAPSHOT-repacked.jar stub.jar
ENTRYPOINT ["java","-jar","stub.jar", "--spring.config.location=file:application.properties"]