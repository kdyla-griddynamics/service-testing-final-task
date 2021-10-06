package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.MOCKED_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getRESTSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.griddynamics.gridu.qa.gateway.api.model.Card;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import com.griddynamics.gridu.qa.payment.service.DtoConverter;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdatePaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(UpdatePaymentTest.class);

  private final DtoConverter dtoConverter = new DtoConverter();

  @BeforeClass(alwaysRun = true)
  public void startWireMock() throws JsonProcessingException {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(MOCKED_PORT));
    wireMockServer.start();
    createStubs();
  }

  @AfterClass(alwaysRun = true)
  public void stopWiremock() {
    wireMockServer.stop();
  }

  @Test
  public void canUpdateExistingPayment() throws JsonProcessingException {
    logger.info("Update existing payment with mocked token");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("ULeonard Hofstadter");
    paymentToUpdate.setCardNumber("1000999988887777");
    paymentToUpdate.setExpiryMonth(9);
    paymentToUpdate.setExpiryYear(2032);
    paymentToUpdate.setCvv("099");
    paymentToUpdate.setId(4L);

    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToUpdate);
    Card card = dtoConverter.convertToCard(paymentModel);

    Response response = given().spec(getRESTSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(200)
        .extract().response();

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.containing(jsonWriter.writeValueAsString(card))));

    Payment paymentFromResponse = response.as(Payment.class);

    assertThat(paymentFromResponse).usingRecursiveComparison()
        .ignoringFields("verified")
        .isEqualTo(paymentToUpdate);

    assertThat(paymentFromResponse.getVerified()).isTrue();
  }

  @Test
  public void cannotUpdateNonExistingPayment() throws JsonProcessingException {
    logger.info("Cannot update non-existing payment");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("UULeonard Hofstadter");
    paymentToUpdate.setCardNumber("2000999988887777");
    paymentToUpdate.setExpiryMonth(10);
    paymentToUpdate.setExpiryYear(2033);
    paymentToUpdate.setCvv("098");
    paymentToUpdate.setId(Long.MAX_VALUE);

    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToUpdate);
    Card card = dtoConverter.convertToCard(paymentModel);

    given().spec(getRESTSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(404);

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.containing(jsonWriter.writeValueAsString(card))));
  }


  @Test
  public void cannotUpdatePaymentWithoutId() {
    logger.info("Cannot update payment without providing id");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("UUULeonard Hofstadter");
    paymentToUpdate.setCardNumber("3000999988887777");
    paymentToUpdate.setExpiryMonth(11);
    paymentToUpdate.setExpiryYear(2034);
    paymentToUpdate.setCvv("199");

    given().spec(getRESTSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(400);

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH)));
  }
}
