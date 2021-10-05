package com.griddynamics.gridu.qa.user.restmocked;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.DEFAULT_UM_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.MOCKED_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.CreateUserRequest;
import com.griddynamics.gridu.qa.user.DeleteUserRequest;
import com.griddynamics.gridu.qa.user.GetUserDetailsRequest;
import com.griddynamics.gridu.qa.user.GetUserDetailsResponse;
import com.griddynamics.gridu.qa.user.UpdateUserRequest;
import com.griddynamics.gridu.qa.user.UserDetails;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import io.restassured.response.Response;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PaymentManagementNotRespondingTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(PaymentManagementNotRespondingTest.class);
  private static final String PAYMENT_PATH = "/payment/([0-9]*)";
  private final DtoConverter dtoConverter = new DtoConverter();

  @BeforeClass(alwaysRun = true)
  public void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(MOCKED_PORT));
    wireMockServer.start();
    createAllStubs();
  }

  @AfterClass(alwaysRun = true)
  public void removeStubs() {
    wireMockServer.resetMappings();
  }

  @Test()
  public void getUserDetailsWhenPaymentManagementServiceIsDown() {
    logger.info("PM mocked: payment not found. Get User Details");

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(3);

    InputStream responseInputStream = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();
    wireMockServer.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(PAYMENT_PATH)));

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    UserDetails receivedUserDetails = getUserDetailsResponse.getUserDetails();
    UserModel receivedUserModel = dtoConverter.convertUserDetails(receivedUserDetails);

    assertThat(receivedUserModel).hasNoNullFieldsOrProperties();
    assertThat(receivedUserDetails.getAddresses().getAddress()).isNotEmpty();
    assertThat(receivedUserDetails.getPayments().getPayment()).isEmpty();
  }

  @Test()
  public void createUserWhenPaymentManagementServiceIsDown() {
    logger.info("PM mocked: payment not found. Create User");

    CreateUserRequest createUserRequest = getCreateUserRequestWithPayment(createNewPayment());

    Response response = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();
    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlPathMatching(PAYMENT_PATH)));

    String responseFaultMessage = response.xmlPath().getString("Envelope.Body.Fault.faultstring");
    assertThat(responseFaultMessage).isEqualTo("Can not save user payments!");
  }

  @Test()
  public void updateUserWhenPaymentManagementServiceIsDown() {
    logger.info("PM mocked: payment not found. Update User");

    long id = 2;
    UserDetails userDetailsBeforeUpdate = getUserDetailsForGivenId(id);

    UpdateUserRequest updateUserRequest = getUpdateUserRequest(id);

    Response response = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();
    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlPathMatching(PAYMENT_PATH)));

    String responseFaultMessage = response.xmlPath().getString("Envelope.Body.Fault.faultstring");
    assertThat(responseFaultMessage).isEqualTo("Can not save user payments!");

    UserDetails userDetailsAfterUpdate = getUserDetailsForGivenId(id);

    UserModel userModelBeforeUpdate = dtoConverter.convertUserDetails(userDetailsBeforeUpdate);
    UserModel userModelAfterUpdate = dtoConverter.convertUserDetails(userDetailsAfterUpdate);

    assertThat(userModelAfterUpdate).usingRecursiveComparison().isEqualTo(userModelBeforeUpdate);
  }

  @Test()
  public void deleteUserWhenPaymentManagementServiceIsDown() {
    logger.info("PM mocked: payment not found. Delete User");

    long id = 1;

    UserDetails userDetailsBeforeDeletion = getUserDetailsForGivenId(id);
    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(id);

    Response response = given(getSpecForPort(DEFAULT_UM_PORT))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();
    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlPathMatching(PAYMENT_PATH)));

    String responseFaultMessage = response.xmlPath().getString("Envelope.Body.Fault.faultstring");
    assertThat(responseFaultMessage).isEqualTo("Can not delete user's payments and/or addresses");

    UserDetails userDetailsAfterDeletion = getUserDetailsForGivenId(id);

    UserModel userModelBeforeDeletion = dtoConverter.convertUserDetails(userDetailsBeforeDeletion);
    UserModel userModelAfterDeletion = dtoConverter.convertUserDetails(userDetailsAfterDeletion);

    assertThat(userModelAfterDeletion).usingRecursiveComparison()
        .isEqualTo(userModelBeforeDeletion);
  }

  private void createAllStubs() {
    wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(PAYMENT_PATH))
        .willReturn(WireMock.aResponse()
            .withStatus(404)
            .withStatusMessage("Payment not found - mocked")));
    wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching(PAYMENT_PATH))
        .willReturn(WireMock.aResponse()
            .withStatus(404)
            .withStatusMessage("Payment not found - mocked")));
    wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(PAYMENT_PATH))
        .willReturn(WireMock.aResponse()
            .withStatus(404)
            .withStatusMessage("Payment not found - mocked")));
  }
}
