package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_UM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.DeleteUserRequest;
import com.griddynamics.gridu.qa.user.UserDetails;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

@Test(groups = "e2e")
public class DeleteUserTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(DeleteUserTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void deleteExistingUserShouldReturn200() {
    logger.info("delete existing user");

    long id = 4;
    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(id);

    given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200);

    UserDetails deletedUserDetails = getUserDetailsForGivenId(id);

    UserModel deletedUserModel = dtoConverter
        .convertUserDetails(deletedUserDetails);

    assertThat(deletedUserModel).hasFieldOrPropertyWithValue("name", "NOT_FOUND");
    assertThat(deletedUserModel).hasFieldOrPropertyWithValue("lastName", "NOT_FOUND");
    assertThat(deletedUserModel).hasFieldOrPropertyWithValue("email", "NOT_FOUND");
  }

  @Test
  public void deleteNonExistingUserShouldReturnError() {
    logger.info("cannot delete non-existing user");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(Integer.MAX_VALUE);

    Response response  = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500)
        .extract().response();

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("User with given id does not exist!");
  }

  @Test
  public void deleteUserWithAddressOrPaymentShouldReturnError() {
    logger.info("cannot delete user with address or payment");

    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(1);

    Response response = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500)
        .extract().response();

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not delete user's payments and/or addresses");
  }
}
