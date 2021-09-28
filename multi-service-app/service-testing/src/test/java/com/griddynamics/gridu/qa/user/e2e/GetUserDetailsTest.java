package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_UM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.GetUserDetailsRequest;
import com.griddynamics.gridu.qa.user.GetUserDetailsResponse;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class GetUserDetailsTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(GetUserDetailsTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void getUserWithExistingIdShouldReturnUserDetails() {
    logger.info("get user with existing id");

    long id = 1;

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(id);

    InputStream responseInputStream = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(getUserDetailsResponse.getUserDetails());

    assertThat(userModelFromResponse.getId()).isEqualTo(id);
    assertThat(userModelFromResponse).hasNoNullFieldsOrProperties();
  }

  @Test
  public void getUserWithNonExistingIdShouldReturnNotFoundUser() {
    logger.info("get user with non-existing id");

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(Integer.MAX_VALUE);

    InputStream responseInputStream = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(getUserDetailsResponse.getUserDetails());

    assertThat(userModelFromResponse).hasFieldOrPropertyWithValue("name", "NOT_FOUND");
    assertThat(userModelFromResponse).hasFieldOrPropertyWithValue("lastName", "NOT_FOUND");
    assertThat(userModelFromResponse).hasFieldOrPropertyWithValue("email", "NOT_FOUND");
  }
}
