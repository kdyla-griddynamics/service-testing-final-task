package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getRESTSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.payment.api.model.Payment;
import java.util.List;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class DeletePaymentTest extends PaymentApiBaseTest {

  private static final Logger logger = Logger.getLogger(DeletePaymentTest.class);

  @Test
  public void canDeletePaymentsByExistingUserId() {
    logger.info("Delete payments of existing user by id");

    long userId = 2L;

    List<Payment> paymentsListBeforeDeletion = getPaymentsByUserId(userId);

    given().spec(getRESTSpecForPort(appPort))
        .log().all()
        .when()
        .delete(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(204);

    List<Payment> paymentsListAfterDeletion = getPaymentsByUserId(userId);

    assertThat(paymentsListBeforeDeletion).isNotEmpty();
    assertThat(paymentsListAfterDeletion).isEmpty();
  }

  @Test
  public void cannotDeletePaymentsByNonExistingUserId() {
    logger.info("Cannot delete payments by non-existing user id");

    long userId = Integer.MAX_VALUE;

    given().spec(getRESTSpecForPort(appPort))
        .log().all()
        .when()
        .delete(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(404);
  }

  @Test
  public void cannotDeletePaymentsByInvalidUserId() {
    logger.info("Cannot delete payments by invalid user id");

    given().spec(getRESTSpecForPort(appPort))
        .log().all()
        .when()
        .delete(String.format("%s/%s", PAYMENT_PATH, "invalidId"))
        .then().log().all()
        .assertThat().statusCode(400);
  }
}
