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
import java.util.List;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testng.annotations.Test;

public class GetPaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(GetPaymentTest.class);

  @Test
  public void canGetPaymentsByExistingUserId() {
    logger.info("Get a list of payments of existing user by id");

    long userId = 1L;

    Response response = given().spec(getSpecForPort(appPort))
        .log().all()
        .when()
        .get(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(200)
        .extract().response();

    List<Payment> paymentsListFromResponse = response.getBody().jsonPath()
        .getList(".", Payment.class);

    assertThat(paymentsListFromResponse).doesNotContainNull();
    SoftAssertions assertSoftly = new SoftAssertions();
    paymentsListFromResponse
        .forEach(payment -> assertSoftly.assertThat(payment.getUserId()).isEqualTo(userId));
    assertSoftly.assertAll();
  }

  @Test
  public void cannotGetPaymentsByNonExistingUserId() {
    logger.info("Cannot get payments by non-existing user id");

    long userId = Integer.MAX_VALUE;

    given().spec(getSpecForPort(appPort))
        .log().all()
        .when()
        .get(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(404);
  }

  @Test
  public void cannotGetPaymentsByInvalidUserId() {
    logger.info("Cannot get payments by invalid user id");

    given().spec(getSpecForPort(appPort))
        .log().all()
        .when()
        .get(String.format("%s/%s", PAYMENT_PATH, "invalidId"))
        .then().log().all()
        .assertThat().statusCode(400);
  }

}
