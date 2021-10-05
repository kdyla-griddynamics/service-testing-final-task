package com.griddynamics.gridu.qa.user.restmocked;

import static com.griddynamics.gridu.qa.util.SOAPWrappers.extractResponseOfGivenType;
import static com.griddynamics.gridu.qa.util.SOAPWrappers.getRequestOfGivenType;
import static com.griddynamics.gridu.qa.util.ServicesConstants.GET_USER_DETAILS_RESPONSE_LOCALNAME;
import static com.griddynamics.gridu.qa.util.ServicesConstants.getSpecForPort;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.griddynamics.gridu.qa.payment.ApiException;
import com.griddynamics.gridu.qa.payment.api.PaymentApi;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
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

public class PaymentManagementNotRespondingTest extends BaseTest {

  private static final Logger logger = Logger.getLogger(PaymentManagementNotRespondingTest.class);

  @MockBean
  private PaymentApi paymentApi;

  private final DtoConverter dtoConverter = new DtoConverter();

  @Test()
  public void getUserDetailsWhenPaymentManagementServiceIsDown() throws ApiException {
    logger.info("PM mocked: payment not found. Get User Details");

    long userId = 3;

    GetUserDetailsRequest getUserDetailsRequest = getGetUserDetailsRequest(userId);

    Mockito.when(paymentApi.getPaymentsByUserId(userId)).thenReturn(null);

    InputStream responseInputStream = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(GetUserDetailsRequest.class, getUserDetailsRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(200)
        .and()
        .extract().asInputStream();

    Mockito.verify(paymentApi).getPaymentsByUserId(userId);

    GetUserDetailsResponse getUserDetailsResponse = extractResponseOfGivenType(responseInputStream,
        GetUserDetailsResponse.class, GET_USER_DETAILS_RESPONSE_LOCALNAME);

    UserDetails receivedUserDetails = getUserDetailsResponse.getUserDetails();
    UserModel receivedUserModel = dtoConverter.convertUserDetails(receivedUserDetails);

    assertThat(receivedUserModel).hasNoNullFieldsOrProperties();
    assertThat(receivedUserDetails.getAddresses().getAddress()).isNotEmpty();
    assertThat(receivedUserDetails.getPayments().getPayment()).isEmpty();
  }

  @Test()
  public void createUserWhenPaymentManagementServiceIsDown() throws ApiException {
    logger.info("PM mocked: payment not found. Create User");

    CreateUserRequest createUserRequest = getCreateUserRequestWithPayment(createNewPayment());

    Mockito.doThrow(ApiException.class).when(paymentApi)
        .updatePaymentsByUserId(Mockito.anyLong(), Mockito.anyList());

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(CreateUserRequest.class, createUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(paymentApi)
        .updatePaymentsByUserId(Mockito.anyLong(), Mockito.anyList());

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not save user payments!");
  }

  @Test()
  public void updateUserWhenPaymentManagementServiceIsDown() throws ApiException {
    logger.info("PM mocked: payment not found. Update User");

    long userId = 2;

    UserDetails userDetailsBeforeUpdate = getUserDetailsForGivenId(userId);
    UpdateUserRequest updateUserRequest = getUpdateUserRequest(userId);

    List<Payment> paymentList = dtoConverter
        .convertPayments(updateUserRequest.getUserDetails().getPayments(), userId);

    Mockito.doThrow(ApiException.class).when(paymentApi)
        .updatePaymentsByUserId(userId, paymentList);

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(UpdateUserRequest.class, updateUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(paymentApi)
        .updatePaymentsByUserId(userId, paymentList);

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not save user payments!");

    UserDetails userDetailsAfterUpdate = getUserDetailsForGivenId(userId);

    UserModel userModelBeforeUpdate = dtoConverter.convertUserDetails(userDetailsBeforeUpdate);
    UserModel userModelAfterUpdate = dtoConverter.convertUserDetails(userDetailsAfterUpdate);

    assertThat(userModelAfterUpdate).usingRecursiveComparison().isEqualTo(userModelBeforeUpdate);
  }

  @Test()
  public void deleteUserWhenPaymentManagementServiceIsDown() throws ApiException {
    logger.info("PM mocked: payment not found. Delete User");

    long userId = 1;

    UserDetails userDetailsBeforeDeletion = getUserDetailsForGivenId(userId);
    DeleteUserRequest deleteUserRequest = getDeleteUserRequest(userId);

    Mockito.doThrow(ApiException.class).when(paymentApi)
        .deleteAllUserPayments(userId);

    Response response = given(getSpecForPort(appPort))
        .body(getRequestOfGivenType(DeleteUserRequest.class, deleteUserRequest))
        .when()
        .post()
        .then().log().all()
        .assertThat().statusCode(500)
        .and()
        .extract().response();

    Mockito.verify(paymentApi)
        .deleteAllUserPayments(userId);

    String responseFaultMessage = getFaultMessage(response);
    assertThat(responseFaultMessage).isEqualTo("Can not delete user's payments and/or addresses");

    UserDetails userDetailsAfterDeletion = getUserDetailsForGivenId(userId);

    UserModel userModelBeforeDeletion = dtoConverter.convertUserDetails(userDetailsBeforeDeletion);
    UserModel userModelAfterDeletion = dtoConverter.convertUserDetails(userDetailsAfterDeletion);

    assertThat(userModelAfterDeletion).usingRecursiveComparison()
        .isEqualTo(userModelBeforeDeletion);
  }
}
