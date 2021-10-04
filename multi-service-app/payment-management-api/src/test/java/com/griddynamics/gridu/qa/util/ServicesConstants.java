package com.griddynamics.gridu.qa.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class ServicesConstants {

  private static final String BASE_URI = "http://localhost:%s";
  public static final String PAYMENT_PATH = "/payment";

  public static RequestSpecification getSpecForPort(int portNumber) {
    return new RequestSpecBuilder()
        .setBaseUri(String.format(BASE_URI, portNumber)).setContentType(ContentType.JSON)
        .build();
  }

  private ServicesConstants(){}

}
