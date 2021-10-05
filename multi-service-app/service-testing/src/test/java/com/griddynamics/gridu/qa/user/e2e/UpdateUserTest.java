package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.UPDATE_USER_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.UpdateUserRequest;
import com.griddynamics.gridu.qa.user.UpdateUserResponse;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import io.restassured.response.Response;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class UpdateUserTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(UpdateUserTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void updateExistingUserShouldReturnResponse() {
    logger.info("update existing user details");

    UpdateUserRequest updateUserRequest = getUpdateUserRequest(2);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    UpdateUserResponse updateUserResponse = extractResponseOfGivenType(responseInputStream,
        UpdateUserResponse.class, UPDATE_USER_RESPONSE_LOCALNAME);

    UserModel userModelFromRequest = dtoConverter
        .convertUserDetails(updateUserRequest.getUserDetails());
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(updateUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().isEqualTo(userModelFromRequest);
  }

  @Test
  public void updateNonExistingUserShouldReturnError() {
    logger.info("cannot update non existing user details");

    UpdateUserRequest updateUserRequest = getUpdateUserRequest(Integer.MAX_VALUE);

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500)
        .extract().response();

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("User with given id does not exist!");
  }
}
