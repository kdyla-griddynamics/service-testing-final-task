package com.griddynamics.gridu.qa.payment.db.dao;

import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends CrudRepository<PaymentModel, Long> {

    List<PaymentModel> findPaymentModelByUserId(Long userId);

    void deletePaymentModelByUserId(Long userId);

}
