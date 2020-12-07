package com.griddynamics.gridu.qa.user.controller;

import com.griddynamics.gridu.qa.user.*;
import com.griddynamics.gridu.qa.user.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class UserEndpoint {

    private static final String NAMESPACE_URI = "http://gridu.qa.payment.griddynamics.com/springsoap/gen";

    private final UserManagementService userManagementService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createUserRequest")
    @ResponsePayload
    public CreateUserResponse createUser(@RequestPayload CreateUserRequest request) {
        return userManagementService.createUser(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserDetailsRequest")
    @ResponsePayload
    public GetUserDetailsResponse getUserDetails(@RequestPayload GetUserDetailsRequest request) {
        return userManagementService.getUserDetails(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserRequest")
    @ResponsePayload
    public UpdateUserResponse updateUser(@RequestPayload UpdateUserRequest request) {
        return userManagementService.updateUser(request);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserRequest")
    @ResponsePayload
    public DeleteUserResponse updateUser(@RequestPayload DeleteUserRequest request) {
        return userManagementService.deleteUser(request);
    }
}
