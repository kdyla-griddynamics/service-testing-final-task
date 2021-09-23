package com.griddynamics.gridu.qa.user;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractCreateUserResponse;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getCreateUserRequestSOAP;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import com.griddynamics.gridu.qa.util.ServicesConstants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class CreateUserTest {

  private final RequestSpecification spec = new RequestSpecBuilder()
      .setBaseUri(ServicesConstants.USER_MANAGEMENT_BASE_URI).setContentType("text/xml").build();
  private final DtoConverter dtoConverter = new DtoConverter();


  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestShouldReturnResponse(String name, String lastName, String email)
      throws IOException, ParserConfigurationException, SAXException, JAXBException, SOAPException {
    CreateUserRequest request = getCreateUserRequest(name, lastName, email);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(request))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(request);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  @Parameters({"name", "lastName", "email"})
  @Test
  public void createUserRequestWithAddressShouldReturnResponse(String name, String lastName,
      String email)
      throws IOException, ParserConfigurationException, SAXException, JAXBException, SOAPException {
    NewAddress address = new NewAddress();
    address.setZip("08844");
    address.setState(State.CA);
    address.setCity("Milpitas");
    address.setLine1("620 N. McCarthy Boulevard");
    address.setLine2("Orange County");

    CreateUserRequest request = getCreateUserRequestWithAddress(name, lastName, email, address);

    InputStream responseInputStream = given(spec)
        .body(getCreateUserRequestSOAP(request))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    CreateUserResponse createUserResponse = extractCreateUserResponse(responseInputStream);

    assertThat(createUserResponse.getUserDetails().getAddresses().getAddress()).isNotEmpty();

    ExistingAddress createdAddress = createUserResponse.getUserDetails().getAddresses().getAddress()
        .stream()
        .findFirst()
        .orElseThrow(NoSuchElementException::new);

    assertThat((NewAddress) createdAddress)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(address);

    UserModel userModelFromRequest = dtoConverter.convertNewUser(request);
    UserModel userModelFromResponse = dtoConverter
        .convertUserDetails(createUserResponse.getUserDetails());

    assertThat(userModelFromResponse).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userModelFromRequest);
  }

  private CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    return request;
  }

  private CreateUserRequest getCreateUserRequestWithAddress(String name, String lastName,
      String email, NewAddress newAddress) {
    CreateUserRequest request = getCreateUserRequest(name, lastName, email);
    Addresses addresses = new Addresses();
    addresses.address = new ArrayList<>();
    addresses.address.add(newAddress);
    request.setAddresses(addresses);
    return request;
  }

}
