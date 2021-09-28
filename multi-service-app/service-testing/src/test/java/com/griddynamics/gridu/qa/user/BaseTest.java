package com.griddynamics.gridu.qa.user;

import com.griddynamics.gridu.qa.user.CreateUserRequest.Addresses;
import com.griddynamics.gridu.qa.user.CreateUserRequest.Payments;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

public abstract class BaseTest {

  private static final Logger logger = Logger.getLogger(BaseTest.class);
  protected final String firstName = "Mike";
  protected final String lastName = "Clark";
  protected final String email = "some-email@gmail.com";

  protected CreateUserRequest getCreateUserRequest(String name, String lastName, String email) {
    CreateUserRequest request = new CreateUserRequest();
    request.setName(name);
    request.setLastName(lastName);
    request.setEmail(email);
    request.setBirthday(getXMLDate());
    return request;
  }

  protected CreateUserRequest getCreateUserRequestWithAddress(NewAddress newAddress) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Addresses addresses = new Addresses();
    addresses.getAddress().add(newAddress);
    request.setAddresses(addresses);
    return request;
  }

  protected CreateUserRequest getCreateUserRequestWithPayment(NewPayment newPayment) {
    CreateUserRequest request = getCreateUserRequest(firstName, lastName, email);
    Payments payments = new Payments();
    payments.getPayment().add(newPayment);
    request.setPayments(payments);
    return request;
  }

  protected GetUserDetailsRequest getGetUserDetailsRequest(long id) {
    GetUserDetailsRequest request = new GetUserDetailsRequest();
    request.setUserId(id);
    return request;
  }

  protected DeleteUserRequest getDeleteUserRequest(long id) {
    DeleteUserRequest request = new DeleteUserRequest();
    request.setUserId(id);
    return request;
  }

  protected UpdateUserRequest getUpdateUserRequest(long id) {
    UserDetails userDetailsForUpdate = new UserDetails();
    userDetailsForUpdate.setId(id);
    String firstName = "UMike";
    userDetailsForUpdate.setName(firstName);
    String lastName = "UClark";
    userDetailsForUpdate.setLastName(lastName);
    String email = "usome-email@gmail.com";
    userDetailsForUpdate.setEmail(email);
    userDetailsForUpdate.setBirthday(getXMLDate());
    ExistingAddress updatedAddress = new ExistingAddress();
    updatedAddress.setId(id);
    updatedAddress.setZip("08844");
    updatedAddress.setState(State.MA);
    updatedAddress.setCity("UMilpitas");
    updatedAddress.setLine1("U620 N. McCarthy Boulevard");
    updatedAddress.setLine2("UOrange County");
    UserDetails.Addresses addresses = new UserDetails.Addresses();
    addresses.getAddress().add(updatedAddress);
    userDetailsForUpdate.setAddresses(addresses);
    ExistingPayment updatedPayment = new ExistingPayment();
    updatedPayment.setId(id);
    updatedPayment.setCardholder(String.format("%s %s", firstName, lastName));
    updatedPayment.setCardNumber(RandomStringUtils.randomNumeric(16));
    updatedPayment.setCvv(RandomStringUtils.randomNumeric(3));
    updatedPayment.setExpiryMonth(8);
    updatedPayment.setExpiryYear(2024);
    UserDetails.Payments payments = new UserDetails.Payments();
    payments.getPayment().add(updatedPayment);
    userDetailsForUpdate.setPayments(payments);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUserDetails(userDetailsForUpdate);
    return request;
  }

  protected XMLGregorianCalendar getXMLDate() {
    LocalDate birthday = LocalDate.of(new Random().nextInt(30) + 1960, new Random().nextInt(12) + 1,
        MonthDay.now().getDayOfMonth());
    GregorianCalendar gregorianDate = GregorianCalendar
        .from(birthday.atStartOfDay(ZoneId.ofOffset("", ZoneOffset.ofHours(0))));
    XMLGregorianCalendar xmlGregorianCalendar = null;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianDate);
    } catch (DatatypeConfigurationException e) {
      logger.error(e.getMessage());
    }
    return xmlGregorianCalendar;
  }

}
