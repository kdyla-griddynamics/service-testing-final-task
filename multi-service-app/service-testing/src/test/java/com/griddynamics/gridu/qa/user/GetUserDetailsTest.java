package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.CREATE_USER_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.SPEC;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GetUserDetailsTest {

  private static final Logger logger = Logger.getLogger(GetUserDetailsTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void getUserWithExistingId() {
    logger.info("get user with existing id");

    long id = 1;

    GetUserDetailsRequest getUserDetailsRequest = getUserDetailsRequest(id);

    InputStream responseInputStream = given(SPEC)
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
    assertThat(userModelFromResponse)
        .hasNoNullFieldsOrPropertiesExcept("birthday", "addresses", "payments");

  }

  private GetUserDetailsRequest getUserDetailsRequest(long id) {
    GetUserDetailsRequest request = new GetUserDetailsRequest();
    request.setUserId(id);
    return request;
  }
}
