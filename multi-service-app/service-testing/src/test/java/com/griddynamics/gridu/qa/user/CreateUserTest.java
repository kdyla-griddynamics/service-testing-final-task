package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractCreateUserResponse;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getCreateUserRequestSOAP;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

import com.griddynamics.gridu.qa.util.ServicesConstants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class CreateUserTest {

  private final RequestSpecification spec = new RequestSpecBuilder()
      .setBaseUri(ServicesConstants.USER_MANAGEMENT_BASE_URI).setContentType("text/xml").build();


  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestShouldReturnResponse(String name, String lastName, String email)
      throws IOException, ParserConfigurationException, SAXException, JAXBException, SOAPException {
    CreateUserRequest request = getCreateUserRequest(name, lastName, email);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(request))
        .when()
        .post()
        .then().assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

    assertEquals(createUserResponse.getUserDetails().getName(), name);
    assertEquals(createUserResponse.getUserDetails().getEmail(), email);
    assertEquals(createUserResponse.getUserDetails().getLastName(), lastName);

  }

  private CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    return request;
  }

}
