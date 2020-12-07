package com.griddynamics.gridu.qa.user.service;

import com.griddynamics.gridu.qa.address.api.AddressApi;
import com.griddynamics.gridu.qa.address.api.model.Address;
import com.griddynamics.gridu.qa.payment.ApiException;
import com.griddynamics.gridu.qa.payment.api.PaymentApi;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.user.*;
import com.griddynamics.gridu.qa.user.db.dao.UserRepository;
import com.griddynamics.gridu.qa.user.db.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);
    private static final String NOT_FOUND = "NOT_FOUND";

    private final UserRepository userRepository;
    private final PaymentApi paymentApi;
    private final AddressApi addressApi;
    private final DtoConverter dtoConverter;

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        UserModel newUser = dtoConverter.convertNewUser(request);
        newUser = userRepository.save(newUser);
        Long userId = newUser.getId();

        List<Payment> paymentList = dtoConverter.convertPayments(request.getPayments(), userId);
        paymentList = postPaymentsAndReturnUpdate(paymentList, userId);

        List<Address> addressList = dtoConverter.convertAddresses(request.getAddresses(), userId);
        addressList = postAddressesAndReturnUpdate(addressList, userId);

        UserDetails details = dtoConverter.convertUserDetails(newUser, paymentList, addressList);
        return dtoConverter.convertToNewUserResponse(details);
    }

    public GetUserDetailsResponse getUserDetails(GetUserDetailsRequest request) {
        long userId = request.getUserId();
        Optional<UserModel> userModel = userRepository.findById(userId);

        if (!userModel.isPresent()) {
            return dtoConverter.convertToUserDetailsResponse(createNotFoundUserDetails(userId));
        }

        List<Payment> payments = getUserPayments(userId, null);
        List<Address> addresses = getUserAddresses(userId, null);

        UserDetails details = dtoConverter.convertUserDetails(userModel.get(), payments, addresses);
        return dtoConverter.convertToUserDetailsResponse(details);
    }

    @Transactional
    public UpdateUserResponse updateUser(UpdateUserRequest request) {
        UserDetails userDetails = request.getUserDetails();
        if (!userRepository.existsById(userDetails.getId())) {
            throw new IllegalStateException("User with given id does not exist!");
        }
        UserModel user = dtoConverter.convertUserDetails(userDetails);
        user = userRepository.save(user);
        Long userId = user.getId();

        List<Payment> paymentList = dtoConverter.convertPayments(userDetails.getPayments(), userId);
        paymentList = postPaymentsAndReturnUpdate(paymentList, userId);

        List<Address> addressList = dtoConverter.convertAddresses(userDetails.getAddresses(), userId);
        addressList = postAddressesAndReturnUpdate(addressList, userId);

        UserDetails details = dtoConverter.convertUserDetails(user, paymentList, addressList);
        return dtoConverter.convertToUpdateUserResponse(details);
    }

    @Transactional
    public DeleteUserResponse deleteUser(DeleteUserRequest request) {
        long userId = request.getUserId();
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User with given id does not exist!");
        }

        try {
            paymentApi.deleteAllUserPayments(userId);
            addressApi.deleteAllUserAddresses(userId);
        } catch (Exception e) {
            LOGGER.error("Exception while deleting collections for the user: ", e);
            throw new IllegalStateException("Can not delete user's payments and/or addresses");
        }
        userRepository.deleteById(userId);
        return new DeleteUserResponse();
    }

    private List<Payment> postPaymentsAndReturnUpdate(List<Payment> paymentList, Long userId) {
        try {
            paymentApi.updatePaymentsByUserId(userId, paymentList);
        } catch (Exception e) {
            LOGGER.error("Exception while sending payments to the PaymentManagement service: ", e);
            throw new IllegalStateException("Can not save user payments!");
        }
        // have two call this client second time to retrieve last version of payments
        // it's better to update specs for Payment service to return saved/updated objects
        return getUserPayments(userId, paymentList);
    }

    private List<Payment> getUserPayments(Long userId, List<Payment> defaultList) {
        try {
            return paymentApi.getPaymentsByUserId(userId);
        } catch (Exception e) {
            LOGGER.warn("Exception while retrieving payments from the PaymentManagement service: ", e);
            return defaultList;
        }
    }

    private List<Address> postAddressesAndReturnUpdate(List<Address> addressList, Long userId) {
        try {
            addressApi.updateAddressesByUserId(userId, addressList);
        } catch (Exception e) {
            LOGGER.error("Exception while sending addresses to the AddressManagement service: ", e);
            throw new IllegalStateException("Can not save user addresses!");
        }
        // have two call this client second time to retrieve last version of addresses
        // it's better to update specs for Address service to return saved/updated objects
        return getUserAddresses(userId, addressList);
    }

    private List<Address> getUserAddresses(Long userId, List<Address> defaultList) {
        try {
            return addressApi.getAddressesByUserId(userId);
        } catch (Exception e) {
            LOGGER.warn("Exception while retrieving addresses from the PaymentManagement service: ", e);
            return defaultList;
        }
    }

    private UserDetails createNotFoundUserDetails(long userId) {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(userId);
        userDetails.setName(NOT_FOUND);
        userDetails.setLastName(NOT_FOUND);
        userDetails.setEmail(NOT_FOUND);
        return userDetails;
    }

}
