package com.griddynamics.gridu.qa.payment;

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

}
