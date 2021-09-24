package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getSOAPRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.CREATE_USER_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.USER_MANAGEMENT_BASE_URI;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Payments;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import com.griddynamics.gridu.qa.util.SOAPWrappers;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.InputStream;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateUserTest {

  private static final Logger logger = Logger.getLogger(CreateUserTest.class);
  private final RequestSpecification spec = new RequestSpecBuilder()
      .setBaseUri(USER_MANAGEMENT_BASE_URI).setContentType("text/xml").build();
  private final DtoConverter dtoConverter = new DtoConverter();
  private final String firstName = "Mike";
  private final String lastName = "Clark";
  private final String email = "some-email@gmail.com";

  @Test
  public void createUserRequestShouldReturnResponse() {
    logger.info("create user with no address nor payments");

    CreateUserRequest createUserRequest = getCreateUserRequest(firstName, lastName, email);

    InputStream responseInputStream = given(spec)
        .body(getSOAPRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
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

    InputStream responseInputStream = given(spec)
        .body(getSOAPRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
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

    InputStream responseInputStream = given(spec)
        .body(getSOAPRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractResponseOfGivenType(responseInputStream,
        CreateUserResponse.class, CREATE_USER_RESPONSE_LOCALNAME);

    assertThat(createUserResponse.getUserDetails().getPayments().getPayment()).isNotEmpty();

    ExistingPayment existingPayment = createUserResponse.getUserDetails().getPayments().getPayment()
        .stream()
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

    given(spec)
        .body(getSOAPRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(405);
  }

  @DataProvider
  public Iterator<Object[]> incorrectUserInput() {
    return Stream.of(
        new Object[]{"create user without first name", getCreateUserRequest(null, lastName, email)},
        new Object[]{"create user without last name", getCreateUserRequest(firstName, null, email)},
        new Object[]{"create user without email", getCreateUserRequest(firstName, lastName, null)})
        .iterator();

  }

  private CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    XMLGregorianCalendarImpl birthday = new XMLGregorianCalendarImpl();
    birthday.setDay(MonthDay.now().getDayOfMonth());
    birthday.setMonth(new Random().nextInt(12) + 1);
    birthday.setYear(new Random().nextInt(30) + 1960);
    birthday.setTimezone(0);

    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setBirthday(birthday);
    return request;
  }

  private CreateUserRequest getCreateUserRequestWithAddress(NewAddress newAddress) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Addresses addresses = new Addresses();
    addresses.address = new ArrayList<>();
    addresses.address.add(newAddress);
    request.setAddresses(addresses);
    return request;
  }

  private CreateUserRequest getCreateUserRequestWithPayment(NewPayment newPayment) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Payments payments = new Payments();
    payments.payment = new ArrayList<>();
    payments.payment.add(newPayment);
    request.setPayments(payments);
    return request;
  }
}
