package com.griddynamics.gridu.qa.address.service;

import com.griddynamics.gridu.qa.address.api.model.Address;
import com.griddynamics.gridu.qa.address.db.model.AddressModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoConverter {

    public Address convertTo(AddressModel addressModel) {
        return new Address()
                       .id(addressModel.getId())
                       .userId(addressModel.getUserId())
                       .city(addressModel.getCity())
                       .zip(addressModel.getZip())
                       .state(Address.StateEnum.fromValue(addressModel.getState()))
                       .addressLine1(addressModel.getLine_one())
                       .addressLine2(addressModel.getLine_two());
    }

    public List<Address> convertTo(List<AddressModel> addressModelList) {
        return addressModelList.stream()
                               .map(this::convertTo)
                               .collect(Collectors.toList());
    }

    public AddressModel convertFrom(Address address) {
        return AddressModel.builder()
                           .id(address.getId())
                           .userId(address.getUserId())
                           .city(address.getCity())
                           .zip(address.getZip())
                           .state(address.getState().getValue())
                           .line_one(address.getAddressLine1())
                           .line_two(address.getAddressLine2())
                           .build();
    }

    public List<AddressModel> convertFrom(List<Address> addressList) {
        return addressList.stream()
                          .map(this::convertFrom)
                          .collect(Collectors.toList());
    }
}
