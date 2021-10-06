package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.MOCKED_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getRESTSpecForPort;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.griddynamics.gridu.qa.gateway.api.model.Error;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import io.restassured.response.Response;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

@SpringBootTest(classes = PaymentManagement.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@TestPropertySource("classpath:application-test.properties")
public class PaymentApiBaseTest extends AbstractTestNGSpringContextTests {

  @LocalServerPort
  protected int appPort;

  protected WireMockServer wireMockServer;
  protected ObjectWriter jsonWriter = new ObjectMapper().writer();
  protected final String CARD_VERIFY_PATH = "/card/verify";

  @BeforeSuite
  public void startWiremock() throws JsonProcessingException {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(MOCKED_PORT));
    wireMockServer.start();
    createStubs();
  }

  @AfterSuite
  public void stopWiremock() {
    wireMockServer.stop();
  }

  protected List<Payment> getPaymentsByUserId(long userId) {
    Response response = given().spec(getRESTSpecForPort(appPort))
        .log().all()
        .when()
        .get(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(200)
        .extract().response();

    return response.getBody().jsonPath()
        .getList(".", Payment.class);
  }

  protected String getCorrectCardRegex() {
    return "(\\{\"cardNumber\":\"([0-9]{16})\",\"cardHolder\":\"[a-zA-Z ]+\",\"expiryYear\":([0-9]{2,4}),\"expiryMonth\":([0-9]{1,2}),\"cvv\":\"([0-9]{3})\"\\})";
  }

  protected String getIncorrectCardRegex(){
    return "(\\{\"cardNumber\":(null),\"cardHolder\":(null),\"expiryYear\":(null),\"expiryMonth\":(null),\"cvv\":(null)\\})";
  }

  private void createStubs() throws JsonProcessingException {
    wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.matching(getCorrectCardRegex()))
        .willReturn(WireMock.aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader("Content-Type", "text/plain")
            .withBody("mocked card verification")));
    Error error = new Error();
    error.setErrorCode(HttpStatus.BAD_REQUEST.toString());
    error.setErrorMessage("Mocked error message");
    wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.matching(getIncorrectCardRegex()))
        .willReturn(WireMock.aResponse()
            .withStatus(HttpStatus.BAD_REQUEST.value())
            .withHeader("Content-Type", "application/json")
            .withBody(jsonWriter.writeValueAsString(error))));
  }

}
