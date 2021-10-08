package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSOAPSpecForPort;
import static io.restassured.RestAssured.given;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Payments;
import io.restassured.response.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@SpringBootTest(classes = UserManagement.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@TestPropertySource("classpath:application-test.properties")
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

  @LocalServerPort
  protected int appPort;

  private static final Logger logger = Logger.getLogger(BaseTest.class);
  protected final String firstName = "Mike";
  protected final String lastName = "Clark";
  protected final String email = "some-email@gmail.com";

  protected CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setBirthday(createXMLDate());
    return request;
  }

  protected CreateUserRequest getCreateUserRequestWithAddress(NewAddress newAddress) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Addresses addresses = new Addresses();
    addresses.getAddress().add(newAddress);
    request.setAddresses(addresses);
    return request;
  }

  protected CreateUserRequest getCreateUserRequestWithPayment(NewPayment newPayment) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Payments payments = new Payments();
    payments.getPayment().add(newPayment);
    request.setPayments(payments);
    return request;
  }

  protected GetUserDetailsRequest getGetUserDetailsRequest(long id) {
    GetUserDetailsRequest request = new GetUserDetailsRequest();
    request.setUserId(id);
    return request;
  }

  protected DeleteUserRequest getDeleteUserRequest(long id) {
    DeleteUserRequest request = new DeleteUserRequest();
    request.setUserId(id);
    return request;
  }

  protected UpdateUserRequest getUpdateUserRequest(long id) {
    UserDetails userDetailsForUpdate = new UserDetails();
    userDetailsForUpdate.setId(id);
    String firstName = "UMike";
    userDetailsForUpdate.setName(firstName);
    String lastName = "UClark";
    userDetailsForUpdate.setLastName(lastName);
    String email = "usome-email@gmail.com";
    userDetailsForUpdate.setEmail(email);
    userDetailsForUpdate.setBirthday(createXMLDate());
    ExistingAddress updatedAddress = new ExistingAddress();
    updatedAddress.setId(id);
    updatedAddress.setZip("08844");
    updatedAddress.setState(State.MA);
    updatedAddress.setCity("UMilpitas");
    updatedAddress.setLine1("U620 N. McCarthy Boulevard");
    updatedAddress.setLine2("UOrange County");
    UserDetails.Addresses addresses = new UserDetails.Addresses();
    addresses.getAddress().add(updatedAddress);
    userDetailsForUpdate.setAddresses(addresses);
    ExistingPayment updatedPayment = new ExistingPayment();
    updatedPayment.setId(id);
    updatedPayment.setCardholder(String.format("%s %s", firstName, lastName));
    updatedPayment.setCardNumber(RandomStringUtils.randomNumeric(16));
    updatedPayment.setCvv(RandomStringUtils.randomNumeric(3));
    updatedPayment.setExpiryMonth(8);
    updatedPayment.setExpiryYear(2024);
    UserDetails.Payments payments = new UserDetails.Payments();
    payments.getPayment().add(updatedPayment);
    userDetailsForUpdate.setPayments(payments);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUserDetails(userDetailsForUpdate);
    return request;
  }

  protected NewAddress createNewAddress() {
    NewAddress newAddress = new NewAddress();
    newAddress.setZip("08844");
    newAddress.setState(State.CA);
    newAddress.setCity("Milpitas");
    newAddress.setLine1("620 N. McCarthy Boulevard");
    newAddress.setLine2("Orange County");
    return newAddress;
  }

  protected NewPayment createNewPayment() {
    NewPayment newPayment = new NewPayment();
    newPayment.setCardholder(String.format("%s %s", firstName, lastName));
    newPayment.setCardNumber(RandomStringUtils.randomNumeric(16));
    newPayment.setCvv(RandomStringUtils.randomNumeric(3));
    newPayment.setExpiryMonth(4);
    newPayment.setExpiryYear(2023);
    return newPayment;
  }

  protected XMLGregorianCalendar createXMLDate() {
    LocalDate birthday = LocalDate.of(new Random().nextInt(30) + 1960, new Random().nextInt(12) + 1,
        new Random().nextInt(28) + 1);
    GregorianCalendar gregorianDate = GregorianCalendar
        .from(birthday.atStartOfDay(ZoneId.systemDefault()));
    XMLGregorianCalendar xmlGregorianCalendar = null;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianDate);
    } catch (DatatypeConfigurationException e) {
      logger.error(e.getMessage());
    }
    return xmlGregorianCalendar;
  }

  protected String getFaultMessage(Response response) {
    return response.xmlPath().getString("Envelope.Body.Fault.faultstring");
  }

  protected UserDetails getUserDetailsForGivenId(long id) {
    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(id);

    InputStream responseInputStream = given(getSOAPSpecForPort(appPort))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then()
        .extract().asInputStream();

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    return getUserDetailsResponse.getUserDetails();
  }

}
