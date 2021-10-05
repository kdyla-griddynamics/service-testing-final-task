package com.griddynamics.gridu.qa.user.e2e;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.CREATE_USER_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.CreateUserRequest;
import com.griddynamics.gridu.qa.user.CreateUserResponse;
import com.griddynamics.gridu.qa.user.ExistingAddress;
import com.griddynamics.gridu.qa.user.ExistingPayment;
import com.griddynamics.gridu.qa.user.NewAddress;
import com.griddynamics.gridu.qa.user.NewPayment;
import com.griddynamics.gridu.qa.user.State;
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

public class CreateUserTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(CreateUserTest.class);
  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void createUserRequestShouldReturnResponse() {
    logger.info("create user with no address nor payments");

    CreateUserRequest createUserRequest = getCreateUserRequest(firstName, lastName, email);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractResponseOfGivenType(responseInputStream,
        CreateUserResponse.class, CREATE_USER_RESPONSE_LOCALNAME);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(createUserRequest);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  @Test
  public void createUserRequestWithAddressShouldReturnResponse() {
    logger.info("create user with address");

    NewAddress newAddress = new NewAddress();
    newAddress.setZip("08844");
    newAddress.setState(State.CA);
    newAddress.setCity("Milpitas");
    newAddress.setLine1("620 N. McCarthy Boulevard");
    newAddress.setLine2("Orange County");

    CreateUserRequest createUserRequest = getCreateUserRequestWithAddress(newAddress);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractResponseOfGivenType(responseInputStream,
        CreateUserResponse.class, CREATE_USER_RESPONSE_LOCALNAME);

    assertThat(createUserResponse.getUserDetails().getAddresses().getAddress()).isNotEmpty();

    ExistingAddress createdAddress = createUserResponse.getUserDetails().getAddresses().getAddress()
        .stream()
        .findFirst()
        .orElseThrow(NoSuchElementException::new);

    assertThat((NewAddress) createdAddress)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(newAddress);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(createUserRequest);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  @Test
  public void createUserRequestWithPaymentShouldReturnResponse() {
    logger.info("create user with payment");

    NewPayment newPayment = new NewPayment();
    newPayment.setCardholder(String.format("%s %s", firstName, lastName));
    newPayment.setCardNumber(RandomStringUtils.randomNumeric(16));
    newPayment.setCvv(RandomStringUtils.randomNumeric(3));
    newPayment.setExpiryMonth(4);
    newPayment.setExpiryYear(2023);

    CreateUserRequest createUserRequest = getCreateUserRequestWithPayment(newPayment);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractResponseOfGivenType(responseInputStream,
        CreateUserResponse.class, CREATE_USER_RESPONSE_LOCALNAME);

    assertThat(createUserResponse.getUserDetails().getPayments().getPayment()).isNotEmpty();

    ExistingPayment existingPayment = createUserResponse.getUserDetails().getPayments().getPayment()
        .stream()
        .filter(payment -> payment.getCvv().equals(newPayment.getCvv()))
        .findFirst()
        .orElseThrow(NoSuchElementException::new);

    assertThat((NewPayment) existingPayment)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(newPayment);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(createUserRequest);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  @Test(dataProvider = "incorrectUserInput")
  public void createUserRequestWithMissingDataShouldReturnError(String caseName,
      CreateUserRequest createUserRequest) {
    logger.info(caseName);

    given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().body()
        .assertThat().statusCode(405);
  }

  @DataProvider
  public Iterator<Object[]> incorrectUserInput() {
    return Stream.of(
        new Object[]{"cannot create user without first name",
            getCreateUserRequest(null, lastName, email)},
        new Object[]{"cannot create user without last name",
            getCreateUserRequest(firstName, null, email)},
        new Object[]{"cannot create user without email",
            getCreateUserRequest(firstName, lastName, null)})
        .iterator();

  }
}
