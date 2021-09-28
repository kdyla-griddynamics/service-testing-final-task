package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_AM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_PM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_UM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.MOCKED_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Payments;
import com.griddynamics.gridu.qa.user.config.RestApiClientsConfig;
import com.griddynamics.gridu.qa.user.config.WebServiceConfig;
import com.griddynamics.gridu.qa.user.controller.UserEndpoint;
import com.griddynamics.gridu.qa.user.db.dao.UserRepository;
import com.griddynamics.gridu.qa.user.service.UserManagementService;
import io.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public abstract class BaseTest {

  private static final Logger logger = Logger.getLogger(BaseTest.class);
  protected final String firstName = "Mike";
  protected final String lastName = "Clark";
  protected final String email = "some-email@gmail.com";
  protected WireMockServer wireMockServer;
  private ConfigurableApplicationContext appContext;

  @BeforeGroups(groups = "e2e", alwaysRun = true)
  public void serviceStartForE2E() {
    setUpService(DEFAULT_AM_PORT, DEFAULT_PM_PORT);
  }

  @BeforeGroups(groups = "AM mocked", alwaysRun = true)
  public void serviceStartForAMMocked() {
    setUpService(MOCKED_PORT, DEFAULT_PM_PORT);
  }

  @AfterGroups(groups = {"e2e", "AM mocked"}, alwaysRun = true)
  public void serviceStop() {
    if (appContext != null) {
      appContext.close();
    }
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  protected CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setBirthday(getXMLDate());
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
    userDetailsForUpdate.setBirthday(getXMLDate());
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

  protected XMLGregorianCalendar getXMLDate() {
    LocalDate birthday = LocalDate.of(new Random().nextInt(30) + 1960, new Random().nextInt(12) + 1,
        MonthDay.now().getDayOfMonth());
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

  protected String getFaultMessage(Response response){
    return response.xmlPath().getString("Envelope.Body.Fault.faultstring");
  }

  protected UserDetails getUserDetailsForGivenId(long id) {
    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(id);

    InputStream responseInputStream = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then()
        .extract().asInputStream();

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    return getUserDetailsResponse.getUserDetails();
  }

  protected void setUpService(int addressServicePort, int paymentServicePort) {
    ArrayList<Class<?>> sources = new ArrayList<>();
    sources.add(UserManagementService.class);
    sources.add(UserRepository.class);
    sources.add(UserEndpoint.class);
    sources.add(RestApiClientsConfig.class);
    sources.add(WebServiceConfig.class);
    final SpringApplication userManagement = new SpringApplication(UserManagement.class);
    userManagement.addPrimarySources(sources);

    Properties userManagementProperties = new Properties();
    userManagementProperties.setProperty("address.service.url",
        String.format("http://localhost:%d", addressServicePort));
    userManagementProperties.setProperty("payment.service.url",
        String.format("http://localhost:%d", paymentServicePort));

    final ConfigurableEnvironment env = new StandardEnvironment();
    try {
      env.getPropertySources()
          .addLast(new ResourcePropertySource("classpath:application-test.properties"));
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
    env.getPropertySources()
        .addLast(new PropertiesPropertySource("serviceProps", userManagementProperties));

    userManagement.setEnvironment(env);
    appContext = userManagement.run();
  }

}
