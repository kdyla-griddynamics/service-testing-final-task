package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getRESTSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.griddynamics.gridu.qa.gateway.ApiException;
import com.griddynamics.gridu.qa.gateway.api.model.Card;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import com.griddynamics.gridu.qa.payment.service.DtoConverter;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class CreatePaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(CreatePaymentTest.class);

  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void canCreateCorrectPayment() throws JsonProcessingException {
    logger.info("Create correct payment with mocked token");

    Payment paymentToCreate = new Payment();
    paymentToCreate.setUserId(1L);
    paymentToCreate.setCardHolder("Leonard Hofstadter");
    paymentToCreate.setCardNumber("0000999988887777");
    paymentToCreate.setExpiryMonth(8);
    paymentToCreate.setExpiryYear(2022);
    paymentToCreate.setCvv("098");

    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToCreate);
    Card card = dtoConverter.convertToCard(paymentModel);

    Response response = given().spec(getRESTSpecForPort(appPort))
        .body(paymentToCreate)
        .log().body()
        .when()
        .post(PAYMENT_PATH)
        .then().log().body()
        .assertThat().statusCode(201)
        .extract().response();

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.containing(jsonWriter.writeValueAsString(card))));

    Payment paymentFromResponse = response.as(Payment.class);

    assertThat(paymentFromResponse).usingRecursiveComparison()
        .ignoringFields("id", "verified")
        .isEqualTo(paymentToCreate);

    assertThat(paymentFromResponse.getVerified()).isTrue();
  }

  @Test
  public void cannotCreateIncorrectPayment() throws JsonProcessingException {
    logger.info("Create empty payment with mocked token");

    Payment emptyPayment = new Payment();
    PaymentModel paymentModel = dtoConverter.convertFrom(emptyPayment);
    Card card = dtoConverter.convertToCard(paymentModel);

    given().spec(getRESTSpecForPort(appPort))
        .body(emptyPayment)
        .log().body()
        .when()
        .post(PAYMENT_PATH)
        .then().log().ifError()
        .assertThat().statusCode(405);

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH))
        .withRequestBody(WireMock.containing(jsonWriter.writeValueAsString(card))));
  }

  @Test
  public void createPaymentRequestWithoutBodyShouldReturnException() throws ApiException {
    logger.info("Create payment request without body should throw API Exception");

    given().spec(getRESTSpecForPort(appPort))
        .log().all()
        .when()
        .post(PAYMENT_PATH)
        .then().log().ifError()
        .assertThat().statusCode(400);

    wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CARD_VERIFY_PATH)));
  }

}
