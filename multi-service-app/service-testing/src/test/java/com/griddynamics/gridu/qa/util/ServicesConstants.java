package com.griddynamics.gridu.qa.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class ServicesConstants {

  public static final int DEFAULT_PORT = 8080;
  public static final int MOCK_PORT = 8886;
  private static final String USER_MANAGEMENT_BASE_URI = "http://localhost:%s/ws/users.wsdl";
  public static final String USER_MANAGEMENT_NAMESPACE = "http://gridu.qa.payment.griddynamics.com/springsoap/gen";
  public static final String CREATE_USER_RESPONSE_LOCALNAME = "createUserResponse";
  public static final String GET_USER_DETAILS_RESPONSE_LOCALNAME = "getUserDetailsResponse";
  public static final String UPDATE_USER_RESPONSE_LOCALNAME = "updateUserResponse";
  public static final String DELETE_USER_RESPONSE_LOCALNAME = "deleteUserResponse";

  public static RequestSpecification getSpecForPort(int portNumber) {
    return new RequestSpecBuilder()
        .setBaseUri(String.format(USER_MANAGEMENT_BASE_URI, portNumber)).setContentType("text/xml")
        .build();
  }

}
