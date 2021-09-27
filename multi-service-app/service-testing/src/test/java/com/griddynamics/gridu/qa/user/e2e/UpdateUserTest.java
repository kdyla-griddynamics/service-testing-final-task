package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.UPDATE_USER_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.UpdateUserRequest;
import com.griddynamics.gridu.qa.user.UpdateUserResponse;
import com.griddynamics.gridu.qa.user.UserDetails;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class UpdateUserTest {

  private static final Logger logger = Logger.getLogger(UpdateUserTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void updateExistingUserShouldReturnResponse() {
    logger.info("update existing user details");

    UpdateUserRequest updateUserRequest = getUpdateUserRequest(2);

    InputStream responseInputStream = given(getSpecForPort(DEFAULT_PORT))
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

    given(getSpecForPort(DEFAULT_PORT))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(500);
  }

  private UpdateUserRequest getUpdateUserRequest(long id) {
    UserDetails userDetailsForUpdate = new UserDetails();
    userDetailsForUpdate.setId(id);
    String firstName = "UMike";
    userDetailsForUpdate.setName(firstName);
    String lastName = "UClark";
    userDetailsForUpdate.setLastName(lastName);
    String email = "usome-email@gmail.com";
    userDetailsForUpdate.setEmail(email);
    userDetailsForUpdate.setBirthday(getXMLDate());
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUserDetails(userDetailsForUpdate);
    return request;
  }

  private XMLGregorianCalendar getXMLDate() {
    LocalDate birthday = LocalDate.of(new Random().nextInt(30) + 1960, new Random().nextInt(12) + 1,
        MonthDay.now().getDayOfMonth());
    GregorianCalendar gregorianDate = GregorianCalendar
        .from(birthday.atStartOfDay(ZoneId.ofOffset("", ZoneOffset.ofHours(0))));
    XMLGregorianCalendar xmlGregorianCalendar = null;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianDate);
    } catch (DatatypeConfigurationException e) {
      logger.error(e.getMessage());
    }
    return xmlGregorianCalendar;
  }
}
