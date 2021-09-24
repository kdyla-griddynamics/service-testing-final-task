package com.griddynamics.gridu.qa.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class ServicesConstants {

  public static final String USER_MANAGEMENT_BASE_URI = "http://localhost:8080/ws/users.wsdl";
  public static final String USER_MANAGEMENT_NAMESPACE = "http://gridu.qa.payment.griddynamics.com/springsoap/gen";
  public static final String CREATE_USER_RESPONSE_LOCALNAME = "createUserResponse";
  public static final String GET_USER_DETAILS_RESPONSE_LOCALNAME = "getUserDetailsResponse";
  public static final String UPDATE_USER_RESPONSE_LOCALNAME = "updateUserResponse";
  public static final String DELETE_USER_RESPONSE_LOCALNAME = "deleteUserResponse";
  public static final RequestSpecification SPEC = new RequestSpecBuilder()
      .setBaseUri(USER_MANAGEMENT_BASE_URI).setContentType("text/xml").build();

}
