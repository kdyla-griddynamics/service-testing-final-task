package com.griddynamics.gridu.qa.address.service;

import com.griddynamics.gridu.qa.address.api.AddressApiDelegate;
import com.griddynamics.gridu.qa.address.api.model.Address;
import com.griddynamics.gridu.qa.address.db.model.AddressModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressApiDelegateImpl implements AddressApiDelegate {

    private final AddressManagementService addressManagementService;
    private final DtoConverter dtoConverter;

    @Override
    public ResponseEntity<Address> addAddress(Address body) {
        // TODO base validator + check for non-existing ID
        AddressModel model = dtoConverter.convertFrom(body);
        model = addressManagementService.saveOrUpdateAddress(model);
        return new ResponseEntity<>(dtoConverter.convertTo(model), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Address> updateAddress(Address body) {
        // TODO base validator + check for existing ID
        AddressModel model = dtoConverter.convertFrom(body);
        model = addressManagementService.saveOrUpdateAddress(model);
        return ResponseEntity.ok(dtoConverter.convertTo(model));
    }

    @Override
    public ResponseEntity<List<Address>> getAddressesByUserId(Long userId) {
        List<AddressModel> userAddresses = addressManagementService.getAllAddressesForUser(userId);
        List<Address> convertedAddresses = dtoConverter.convertTo(userAddresses);
        return ResponseEntity.ok(convertedAddresses);
    }

    @Override
    public ResponseEntity<Void> updateAddressesByUserId(Long userId,
                                                        List<Address> addresses) {
        List<AddressModel> userAddresses = dtoConverter.convertFrom(addresses);
        addressManagementService.updateAddressesForUser(userId, userAddresses);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteAllUserAddresses(Long userId) {
        addressManagementService.deleteAllAddressesForUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
