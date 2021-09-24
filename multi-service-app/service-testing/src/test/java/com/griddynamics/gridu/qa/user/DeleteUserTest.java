package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.SPEC;
import static io.restassured.RestAssured.given;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class DeleteUserTest {

  private static final Logger logger = Logger.getLogger(DeleteUserTest.class);

  @Test
  public void deleteExistingUserShouldReturn200() {
    logger.info("delete existing user");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(4);

    given(SPEC)
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200);
  }

  @Test
  public void deleteNonExistingUserShouldReturnError() {
    logger.info("cannot delete non-existing user");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(Integer.MAX_VALUE);

    given(SPEC)
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500);
  }

  @Test
  public void deleteUserWithAddressOrPaymentShouldReturnError() {
    logger.info("cannot delete user with address or payment");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(1);

    given(SPEC)
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500);
  }

  private DeleteUserRequest getDeleteUserRequest(long id) {
    DeleteUserRequest request = new DeleteUserRequest();
    request.setUserId(id);
    return request;
  }
}
