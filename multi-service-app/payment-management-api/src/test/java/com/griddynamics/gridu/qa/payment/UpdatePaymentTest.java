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

public class UpdatePaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(UpdatePaymentTest.class);

  @MockBean
  private CardApi cardApi;

  private final DtoConverter dtoConverter = new DtoConverter();

  @Test
  public void canUpdateExistingPayment() throws ApiException {
    logger.info("Update existing payment with mocked token");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("ULeonard Hofstadter");
    paymentToUpdate.setCardNumber("1000999988887777");
    paymentToUpdate.setExpiryMonth(9);
    paymentToUpdate.setExpiryYear(2032);
    paymentToUpdate.setCvv("099");
    paymentToUpdate.setId(1L);

    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToUpdate);
    Card card = dtoConverter.convertToCard(paymentModel);

    Mockito.when(cardApi.verifyCard(card)).thenReturn("mocked payment verified");

    Response response = given().spec(getSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(200)
        .extract().response();

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(card);

    Payment paymentFromResponse = response.as(Payment.class);

    assertThat(paymentFromResponse).usingRecursiveComparison()
        .ignoringFields("verified")
        .isEqualTo(paymentToUpdate);

    assertThat(paymentFromResponse.getVerified()).isTrue();
  }

  @Test
  public void cannotUpdateNonExistingPayment() throws ApiException {
    logger.info("Cannot update non-existing payment");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("ULeonard Hofstadter");
    paymentToUpdate.setCardNumber("1000999988887777");
    paymentToUpdate.setExpiryMonth(9);
    paymentToUpdate.setExpiryYear(2032);
    paymentToUpdate.setCvv("099");
    paymentToUpdate.setId(Long.MAX_VALUE);
    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToUpdate);
    Card card = dtoConverter.convertToCard(paymentModel);

    Mockito.when(cardApi.verifyCard(card)).thenReturn("mocked payment verified");

    given().spec(getSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(404);

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(card);
  }


  @Test
  public void cannotUpdatePaymentWithoutId() throws ApiException {
    logger.info("Cannot update payment without providing id");

    Payment paymentToUpdate = new Payment();
    paymentToUpdate.setUserId(1L);
    paymentToUpdate.setCardHolder("ULeonard Hofstadter");
    paymentToUpdate.setCardNumber("1000999988887777");
    paymentToUpdate.setExpiryMonth(9);
    paymentToUpdate.setExpiryYear(2032);
    paymentToUpdate.setCvv("099");
    PaymentModel paymentModel = dtoConverter.convertFrom(paymentToUpdate);
    Card card = dtoConverter.convertToCard(paymentModel);

    Mockito.when(cardApi.verifyCard(card)).thenReturn("mocked payment verified");

    given().spec(getSpecForPort(appPort))
        .body(paymentToUpdate)
        .log().all()
        .when()
        .put(PAYMENT_PATH)
        .then().log().all()
        .assertThat().statusCode(400);

    Mockito.verify(cardApi, Mockito.times(1)).verifyCard(card);
  }

}
