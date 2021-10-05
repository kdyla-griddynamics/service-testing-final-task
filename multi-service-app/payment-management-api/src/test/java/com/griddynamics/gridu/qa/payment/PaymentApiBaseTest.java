package com.griddynamics.gridu.qa.payment;

import static com.griddynamics.gridu.qa.util.ServicesConstants.PAYMENT_PATH;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;

import com.griddynamics.gridu.qa.payment.api.model.Payment;
import io.restassured.response.Response;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@SpringBootTest(classes = PaymentManagement.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@TestPropertySource("classpath:application-test.properties")
public class PaymentApiBaseTest extends AbstractTestNGSpringContextTests {

  @LocalServerPort
  protected int appPort;

  protected List<Payment> getPaymentsByUserId(long userId){
    Response response = given().spec(getSpecForPort(appPort))
        .log().all()
        .when()
        .get(String.format("%s/%s", PAYMENT_PATH, userId))
        .then().log().all()
        .assertThat().statusCode(200)
        .extract().response();

    return response.getBody().jsonPath()
        .getList(".", Payment.class);
  }

}
