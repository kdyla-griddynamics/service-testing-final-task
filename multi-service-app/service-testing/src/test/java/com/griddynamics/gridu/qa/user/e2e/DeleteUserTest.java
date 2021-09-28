package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_UM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;

import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.DeleteUserRequest;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

@Test(groups = "e2e")
public class DeleteUserTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(DeleteUserTest.class);

  @Test
  public void deleteExistingUserShouldReturn200() {
    logger.info("delete existing user");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(4);

    given(getSpecForPort(DEFAULT_UM_PORT))
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

    given(getSpecForPort(DEFAULT_UM_PORT))
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

    given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500);
  }
}
