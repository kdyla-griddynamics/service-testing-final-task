package com.griddynamics.gridu.qa.user.restmocked;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.address.ApiException;
import com.griddynamics.gridu.qa.address.api.AddressApi;
import com.griddynamics.gridu.qa.address.api.model.Address;
import com.griddynamics.gridu.qa.user.BaseTest;
import com.griddynamics.gridu.qa.user.CreateUserRequest;
import com.griddynamics.gridu.qa.user.DeleteUserRequest;
import com.griddynamics.gridu.qa.user.GetUserDetailsRequest;
import com.griddynamics.gridu.qa.user.GetUserDetailsResponse;
import com.griddynamics.gridu.qa.user.UpdateUserRequest;
import com.griddynamics.gridu.qa.user.UserDetails;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import com.griddynamics.gridu.qa.user.service.DtoConverter;
import io.restassured.response.Response;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testng.annotations.Test;

public class AddressManagementNotRespondingTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(AddressManagementNotRespondingTest.class);

  @MockBean
  private AddressApi addressApi;

  private final DtoConverter dtoConverter = new DtoConverter();

  @Test()
  public void getUserDetailsWhenAddressManagementServiceIsDown() throws ApiException {
    logger.info("AM mocked: address not found. Get User Details");

    long userId = 3;

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(userId);

    Mockito.when(addressApi.getAddressesByUserId(userId)).thenReturn(null);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    Mockito.verify(addressApi).getAddressesByUserId(userId);

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    UserDetails receivedUserDetails = getUserDetailsResponse.getUserDetails();
    UserModel receivedUserModel = dtoConverter.convertUserDetails(receivedUserDetails);

    assertThat(receivedUserModel).hasNoNullFieldsOrProperties();
    assertThat(receivedUserDetails.getAddresses().getAddress()).isEmpty();
    assertThat(receivedUserDetails.getPayments().getPayment()).isNotEmpty();
  }

  @Test()
  public void createUserWhenAddressManagementServiceIsDown() throws ApiException {
    logger.info("AM mocked: address not found. Create User");

    CreateUserRequest createUserRequest = getCreateUserRequestWithAddress(createNewAddress());

    Mockito.doThrow(ApiException.class).when(addressApi)
        .updateAddressesByUserId(Mockito.anyLong(), Mockito.anyList());

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(addressApi)
        .updateAddressesByUserId(Mockito.anyLong(), Mockito.anyList());

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not save user addresses!");
  }

  @Test()
  public void updateUserWhenAddressManagementServiceIsDown() throws ApiException {
    logger.info("AM mocked: address not found. Update User");

    long userId = 2;

    UserDetails userDetailsBeforeUpdate = getUserDetailsForGivenId(userId);
    UpdateUserRequest updateUserRequest = getUpdateUserRequest(userId);

    List<Address> addressList = dtoConverter
        .convertAddresses(updateUserRequest.getUserDetails().getAddresses(), userId);

    Mockito.doThrow(ApiException.class).when(addressApi)
        .updateAddressesByUserId(userId, addressList);

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(addressApi)
        .updateAddressesByUserId(userId, addressList);

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not save user addresses!");

    UserDetails userDetailsAfterUpdate = getUserDetailsForGivenId(userId);

    UserModel userModelBeforeUpdate = dtoConverter.convertUserDetails(userDetailsBeforeUpdate);
    UserModel userModelAfterUpdate = dtoConverter.convertUserDetails(userDetailsAfterUpdate);

    assertThat(userModelAfterUpdate).usingRecursiveComparison().isEqualTo(userModelBeforeUpdate);
  }

  @Test()
  public void deleteUserWhenAddressManagementServiceIsDown() throws ApiException {
    logger.info("AM mocked: address not found. Delete User");

    long userId = 1;

    UserDetails userDetailsBeforeDeletion = getUserDetailsForGivenId(userId);
    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(userId);

    Mockito.doThrow(ApiException.class).when(addressApi)
        .deleteAllUserAddresses(userId);

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(addressApi)
        .deleteAllUserAddresses(userId);

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not delete user's payments and/or addresses");

    UserDetails userDetailsAfterDeletion = getUserDetailsForGivenId(userId);

    UserModel userModelBeforeDeletion = dtoConverter.convertUserDetails(userDetailsBeforeDeletion);
    UserModel userModelAfterDeletion = dtoConverter.convertUserDetails(userDetailsAfterDeletion);

    assertThat(userModelAfterDeletion).usingRecursiveComparison()
        .isEqualTo(userModelBeforeDeletion);
  }
}
