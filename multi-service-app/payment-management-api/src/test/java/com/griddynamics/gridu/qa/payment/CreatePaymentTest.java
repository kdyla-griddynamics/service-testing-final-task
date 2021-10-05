package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.gateway.ApiException;
import com.griddynamics.gridu.qa.gateway.api.CardApi;
import com.griddynamics.gridu.qa.gateway.api.model.Card;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import com.griddynamics.gridu.qa.payment.service.DtoConverter;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testng.annotations.Test;

public class CreatePaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(CreatePaymentTest.class);

  @MockBean
  private CardApi cardApi;

  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void canCreateCorrectPayment() throws ApiException {
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

    Mockito.when(cardApi.verifyCard(card)).thenReturn("mocked payment verified");

    Response response = given().spec(getSpecForPort(appPort))
        .body(paymentToCreate)
        .log().all()
        .when()
        .post(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(201)
        .extract().response();

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(card);

    Payment paymentFromResponse = response.as(Payment.class);

    assertThat(paymentFromResponse).usingRecursiveComparison()
        .ignoringFields("id", "verified")
        .isEqualTo(paymentToCreate);

    assertThat(paymentFromResponse.getVerified()).isTrue();
  }

  @Test
  public void cannotCreateIncorrectPayment() throws ApiException {
    logger.info("Create empty payment with mocked token");

    Payment emptyPayment = new Payment();
    PaymentModel paymentModel = dtoConverter.convertFrom(emptyPayment);
    Card card = dtoConverter.convertToCard(paymentModel);

    Mockito.when(cardApi.verifyCard(card)).thenReturn(PaymentModel.FAILED_TOKEN);

    given().spec(getSpecForPort(appPort))
        .body(emptyPayment)
        .log().all()
        .when()
        .post(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(405);

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(card);
  }

  @Test
  public void createPaymentRequestWithoutBodyShouldReturnException() throws ApiException {
    logger.info("Create payment request without body should throw API Exception");

    Mockito.when(cardApi.verifyCard(null)).thenThrow(ApiException.class);

    given().spec(getSpecForPort(appPort))
        .log().all()
        .when()
        .post(PAYMENT_PATH)
        .then().log().all();

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(new Card());
  }

}
