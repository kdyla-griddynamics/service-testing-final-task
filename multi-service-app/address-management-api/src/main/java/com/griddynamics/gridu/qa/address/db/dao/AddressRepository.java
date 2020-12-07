package com.griddynamics.gridu.qa.address.db.dao;

import com.griddynamics.gridu.qa.address.db.model.AddressModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends CrudRepository<AddressModel, Long> {

    List<AddressModel> findAddressModelByUserId(Long userId);

    void deleteAddressModelByUserId(Long userId);

}
