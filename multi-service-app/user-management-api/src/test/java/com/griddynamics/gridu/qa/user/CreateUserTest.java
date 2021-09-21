package com.griddynamics.gridu.qa.user;

import static io.restassured.RestAssured.given;

import io.restassured.internal.util.IOUtils;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.testng.annotations.Test;

public class CreateUserTest {

  @Test
  public void createUserRequestShouldReturnResponse() throws IOException {
    File requestFile = new File("src/test/resources/createUserRequest.xml");
    FileInputStream inputStream = new FileInputStream(requestFile);

    Response response = given().baseUri("http://localhost:8080/ws/users.wsdl")
        .header("Content-Type", "text/xml")
        .and()
        .body(IOUtils.toByteArray(inputStream))
        .when()
        .post();

    response.prettyPrint();

  }

}
