package com.griddynamics.gridu.qa.address.service;

import com.griddynamics.gridu.qa.address.db.dao.AddressRepository;
import com.griddynamics.gridu.qa.address.db.model.AddressModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressManagementService {

    private final AddressRepository addressRepository;

    public AddressModel saveOrUpdateAddress(AddressModel addressModel) {
        return addressRepository.save(addressModel);
    }

    public List<AddressModel> getAllAddressesForUser(Long userId) {
        return addressRepository.findAddressModelByUserId(userId);
    }

    @Transactional
    public void updateAddressesForUser(Long userId, List<AddressModel> newAddresses) {
        // easiest way to update the collection - remove all existing, then add new entries again
        addressRepository.deleteAddressModelByUserId(userId);
        addressRepository.saveAll(newAddresses);
    }

    public void deleteAllAddressesForUser(Long userId) {
        addressRepository.deleteAddressModelByUserId(userId);
    }
}
