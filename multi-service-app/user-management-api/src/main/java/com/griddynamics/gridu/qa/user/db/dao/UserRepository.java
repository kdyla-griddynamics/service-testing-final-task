package com.griddynamics.gridu.qa.user.db.dao;

import com.griddynamics.gridu.qa.user.db.model.UserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {

}
