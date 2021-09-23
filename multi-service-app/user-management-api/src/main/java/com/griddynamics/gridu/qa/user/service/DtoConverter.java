package com.griddynamics.gridu.qa.user.service;

import com.griddynamics.gridu.qa.address.api.model.Address;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.user.*;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

@Component
public class DtoConverter {

    public UserModel convertNewUser(CreateUserRequest request) {
        return UserModel.builder()
                        .name(request.getName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .birthday(convertDate(request.getBirthday()))
                        .build();
    }

    public UserModel convertUserDetails(UserDetails userDetails) {
        return UserModel.builder()
                        .id(userDetails.getId())
                        .name(userDetails.getName())
                        .lastName(userDetails.getLastName())
                        .email(userDetails.getEmail())
                        .birthday(convertDate(userDetails.getBirthday()))
                        .build();
    }

    public UserDetails convertUserDetails(UserModel userModel,
                                          List<Payment> payments,
                                          List<Address> addresses) {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(userModel.getId());
        userDetails.setName(userModel.getName());
        userDetails.setLastName(userModel.getLastName());
        userDetails.setEmail(userModel.getEmail());
        userDetails.setBirthday(convertDate(userModel.getBirthday()));

        userDetails.setPayments(convertPayments(payments));
        userDetails.setAddresses(convertAddresses(addresses));

        return userDetails;
    }

    public CreateUserResponse convertToNewUserResponse(UserDetails details) {
        CreateUserResponse response = new CreateUserResponse();
        response.setUserDetails(details);
        return response;
    }

    public GetUserDetailsResponse convertToUserDetailsResponse(UserDetails details) {
        GetUserDetailsResponse response = new GetUserDetailsResponse();
        response.setUserDetails(details);
        return response;
    }

    public UpdateUserResponse convertToUpdateUserResponse(UserDetails details) {
        UpdateUserResponse response = new UpdateUserResponse();
        response.setUserDetails(details);
        return response;
    }

    public List<Payment> convertPayments(CreateUserRequest.Payments payments, Long userId) {
        if (payments == null) return new ArrayList<>();
        return payments.getPayment().stream()
                       .map(p -> convertNewPayment(p, userId))
                       .collect(Collectors.toList());
    }

    public List<Payment> convertPayments(UserDetails.Payments payments, Long userId) {
        if (payments == null) return new ArrayList<>();
        return payments.getPayment().stream()
                       .map(p -> convertPayment(p, userId))
                       .collect(Collectors.toList());
    }

    public UserDetails.Payments convertPayments(List<Payment> payments) {
        UserDetails.Payments responsePayments = new UserDetails.Payments();
        if (payments == null) return responsePayments;

        payments.stream().map(this::convertPayment)
                .forEach(p -> responsePayments.getPayment().add(p));
        return responsePayments;
    }

    private Payment convertNewPayment(NewPayment payment, Long userId) {
        return new Payment()
                       .userId(userId)
                       .cardNumber(payment.getCardNumber())
                       .cardHolder(payment.getCardholder())
                       .expiryYear(payment.getExpiryYear())
                       .expiryMonth(payment.getExpiryMonth())
                       .cvv(payment.getCvv());
    }

    private Payment convertPayment(ExistingPayment payment, Long userId) {
        return convertNewPayment(payment, userId).id(payment.getId());
    }

    private ExistingPayment convertPayment(Payment payment) {
        ExistingPayment existingPayment = new ExistingPayment();
        existingPayment.setId(payment.getId());
        existingPayment.setCardholder(payment.getCardHolder());
        existingPayment.setCardNumber(payment.getCardNumber());
        existingPayment.setExpiryYear(payment.getExpiryYear());
        existingPayment.setExpiryMonth(payment.getExpiryMonth());
        existingPayment.setCvv(payment.getCvv());
        return existingPayment;
    }

    public List<Address> convertAddresses(CreateUserRequest.Addresses addresses, Long userId) {
        if (addresses == null) return new ArrayList<>();
        return addresses.getAddress().stream()
                        .map(a -> convertNewAddress(a, userId))
                        .collect(Collectors.toList());
    }

    public List<Address> convertAddresses(UserDetails.Addresses addresses, Long userId) {
        if (addresses == null) return new ArrayList<>();
        return addresses.getAddress().stream()
                        .map(a -> convertAddress(a, userId))
                        .collect(Collectors.toList());
    }

    public UserDetails.Addresses convertAddresses(List<Address> addresses) {
        UserDetails.Addresses responseAddresses = new UserDetails.Addresses();
        if (addresses == null) return responseAddresses;

        addresses.stream().map(this::convertAddress)
                 .forEach(a -> responseAddresses.getAddress().add(a));
        return responseAddresses;
    }

    private Address convertNewAddress(NewAddress address, Long userId) {
        return new Address()
                       .userId(userId)
                       .state(convertState(address.getState()))
                       .zip(address.getZip())
                       .city(address.getCity())
                       .addressLine1(address.getLine1())
                       .addressLine2(address.getLine2());
    }

    private Address convertAddress(ExistingAddress address, Long userId) {
        return convertNewAddress(address, userId).id(address.getId());
    }

    private ExistingAddress convertAddress(Address address) {
        ExistingAddress existingAddress = new ExistingAddress();
        existingAddress.setId(address.getId());
        existingAddress.setState(convertState(address.getState()));
        existingAddress.setZip(address.getZip());
        existingAddress.setCity(address.getCity());
        existingAddress.setLine1(address.getAddressLine1());
        existingAddress.setLine2(address.getAddressLine2());
        return existingAddress;
    }

    private Address.StateEnum convertState(State state) {
        if (state == null) return null;
        return Address.StateEnum.fromValue(state.value());
    }

    private State convertState(Address.StateEnum state) {
        if (state == null) return null;
        return State.fromValue(state.getValue());
    }

    private Date convertDate(XMLGregorianCalendar gregorianCalendar) {
        if (gregorianCalendar == null) return null;
        return new Date(gregorianCalendar.toGregorianCalendar().getTimeInMillis());
    }

    private XMLGregorianCalendar convertDate(Date date) {
        if (date == null) return null;
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(date.getTime());
            return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Something is not right: " + e.getMessage());
        }
    }
}
