package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.SPEC;
import static com.griddynamics.gridu.qa.util.ServicesConstants.UPDATE_USER_RESPONSE_LOCALNAME;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import java.io.InputStream;
import java.time.MonthDay;
import java.util.Random;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class UpdateUserTest {

  private static final Logger logger = Logger.getLogger(UpdateUserTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void updateExistingUserShouldReturnResponse() {
    logger.info("update existing user details");

    UpdateUserRequest updateUserRequest = getUpdateUserRequest(2);

    InputStream responseInputStream = given(SPEC)
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

    given(SPEC)
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500);
  }

  private UpdateUserRequest getUpdateUserRequest(long id) {
    XMLGregorianCalendarImpl birthday = new XMLGregorianCalendarImpl();
    birthday.setDay(MonthDay.now().getDayOfMonth());
    birthday.setMonth(new Random().nextInt(12) + 1);
    birthday.setYear(new Random().nextInt(30) + 1960);
    birthday.setTimezone(0);
    UserDetails userDetailsForUpdate = new UserDetails();
    userDetailsForUpdate.setId(id);
    String firstName = "UMike";
    userDetailsForUpdate.setName(firstName);
    String lastName = "UClark";
    userDetailsForUpdate.setLastName(lastName);
    String email = "usome-email@gmail.com";
    userDetailsForUpdate.setEmail(email);
    userDetailsForUpdate.setBirthday(birthday);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUserDetails(userDetailsForUpdate);
    return request;
  }
}
