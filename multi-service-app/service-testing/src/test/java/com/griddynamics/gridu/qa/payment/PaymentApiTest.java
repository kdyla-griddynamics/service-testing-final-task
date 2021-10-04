package com.griddynamics.gridu.qa.payment;

import com.griddynamics.gridu.qa.gateway.api.CardApi;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringBootTest(classes = PaymentManagement.class,
    webEnvironment = WebEnvironment.DEFINED_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@TestPropertySource("classpath:application-test.properties")
public class PaymentApiTest extends AbstractTestNGSpringContextTests {

  @Value("${server.port}")
  private int appPort;

  @MockBean
  private CardApi cardApi;

  @Test
  public void createPayment(){
    Assertions.assertThat(appPort).isEqualTo(8886);
  }

}
