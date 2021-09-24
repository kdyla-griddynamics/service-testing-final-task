package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractCreateUserResponse;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getCreateUserRequestSOAP;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Payments;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import com.griddynamics.gridu.qa.util.ServicesConstants;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.InputStream;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class CreateUserTest {

  private final RequestSpecification spec = new RequestSpecBuilder()
      .setBaseUri(ServicesConstants.USER_MANAGEMENT_BASE_URI).setContentType("text/xml").build();
  private final DtoConverter dtoConverter = new DtoConverter();


  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestShouldReturnResponse(String name, String lastName, String email) {
    CreateUserRequest createUserRequest = getCreateUserRequest(name, lastName, email);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(createUserRequest);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestWithAddressShouldReturnResponse(String name, String lastName,
      String email) {
    NewAddress newAddress = new NewAddress();
    newAddress.setZip("08844");
    newAddress.setState(State.CA);
    newAddress.setCity("Milpitas");
    newAddress.setLine1("620 N. McCarthy Boulevard");
    newAddress.setLine2("Orange County");

    CreateUserRequest createUserRequest = getCreateUserRequestWithAddress(name, lastName, email,
        newAddress);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

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

  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestWithPaymentShouldReturnResponse(String name, String lastName,
      String email) {
    NewPayment newPayment = new NewPayment();
    newPayment.setCardholder(String.format("%s %s", name, lastName));
    newPayment.setCardNumber(RandomStringUtils.randomNumeric(16));
    newPayment.setCvv(RandomStringUtils.randomNumeric(3));
    newPayment.setExpiryMonth(4);
    newPayment.setExpiryYear(2023);

    CreateUserRequest createUserRequest = getCreateUserRequestWithPayment(name, lastName, email,
        newPayment);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

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

  private CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    XMLGregorianCalendarImpl birthday = new XMLGregorianCalendarImpl();
    birthday.setDay(MonthDay.now().getDayOfMonth());
    birthday.setMonth(new Random().nextInt(Month.values().length));
    birthday.setYear(new Random().nextInt(30) + 1960);
    birthday.setTimezone(0);

    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setBirthday(birthday);
    return request;
  }

  private CreateUserRequest getCreateUserRequestWithAddress(String name, String lastName,
      String email, NewAddress newAddress) {
    CreateUserRequest request = getCreateUserRequest(name, lastName, email);
    Addresses addresses = new Addresses();
    addresses.address = new ArrayList<>();
    addresses.address.add(newAddress);
    request.setAddresses(addresses);
    return request;
  }

  private CreateUserRequest getCreateUserRequestWithPayment(String name, String lastName,
      String email, NewPayment newPayment) {
    CreateUserRequest request = getCreateUserRequest(name, lastName, email);
    Payments payments = new Payments();
    payments.payment = new ArrayList<>();
    payments.payment.add(newPayment);
    request.setPayments(payments);
    return request;
  }
}
