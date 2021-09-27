package com.griddynamics.gridu.qa.user.restmocked;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.MOCK_PORT;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.griddynamics.gridu.qa.user.GetUserDetailsRequest;
import com.griddynamics.gridu.qa.user.UserManagement;
import com.griddynamics.gridu.qa.user.config.RestApiClientsConfig;
import com.griddynamics.gridu.qa.user.config.WebServiceConfig;
import com.griddynamics.gridu.qa.user.controller.UserEndpoint;
import com.griddynamics.gridu.qa.user.db.dao.UserRepository;
import com.griddynamics.gridu.qa.user.service.UserManagementService;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AddressManagementNotRespondingTest {

  private static final Logger logger = Logger.getLogger(AddressManagementNotRespondingTest.class);
  private WireMockServer wireMockServer;

  @BeforeSuite(alwaysRun = true)
  public void serviceStart() throws IOException {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8888));
    wireMockServer.start();
    logger.info("Wiremock started at port: 8888");

    ArrayList<Class<?>> sources = new ArrayList<>();
    sources.add(UserManagementService.class);
    sources.add(UserRepository.class);
    sources.add(UserEndpoint.class);
    sources.add(RestApiClientsConfig.class);
    sources.add(WebServiceConfig.class);
    final SpringApplication userManagement = new SpringApplication(UserManagement.class);
    userManagement.addPrimarySources(sources);

    final ConfigurableEnvironment env = new StandardEnvironment();
    env.getPropertySources()
        .addLast(new ResourcePropertySource("classpath:application-test.properties"));

    userManagement.setEnvironment(env);
    userManagement.run();
  }

  @Test
  public void getUserDetailsWhenAddressManagementServiceIsDown() {
    logger.info("AM mocked: address not found");
    createAllStubs();

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(1);

    Response response = given(getSpecForPort(MOCK_PORT))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().all().extract().response();

    wireMockServer.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/address/([0-9]*)")));
  }

  @AfterSuite(alwaysRun = true)
  public void serviceStop() {
    wireMockServer.stop();
  }

  private void createAllStubs() {
    wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/address/([0-9]*)"))
        .willReturn(WireMock.aResponse()
            .withStatus(404)
            .withStatusMessage("Address not found - mocked")));
    wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/payment/([0-9]*)"))
        .willReturn(WireMock.aResponse()
            .withStatus(404)
            .withStatusMessage("Payment not found - mocked")));
  }

  private GetUserDetailsRequest getGetUserDetailsRequest(long id) {
    GetUserDetailsRequest request = new GetUserDetailsRequest();
    request.setUserId(id);
    return request;
  }

}
