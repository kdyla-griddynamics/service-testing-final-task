package com.griddynamics.gridu.qa.payment.service;

import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.griddynamics.gridu.qa.payment.api.PaymentApiDelegate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApiDelegateImpl implements PaymentApiDelegate {

    private final PaymentManagementService paymentManagementService;
    private final DtoConverter dtoConverter;

    @Override
    public ResponseEntity<Payment> addPayment(Payment payment) {
        // TODO base validator + check for non-existing ID
        PaymentModel paymentModel = dtoConverter.convertFrom(payment);
        paymentModel = paymentManagementService.saveOrUpdatePayment(paymentModel);
        return new ResponseEntity<>(dtoConverter.convertTo(paymentModel), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Payment> updatePayment(Payment payment) {
        // TODO base validator + check for existing ID
        PaymentModel paymentModel = dtoConverter.convertFrom(payment);
        paymentModel = paymentManagementService.saveOrUpdatePayment(paymentModel);
        return new ResponseEntity<>(dtoConverter.convertTo(paymentModel), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Payment>> getPaymentsByUserId(Long userId) {
        List<PaymentModel> userPayments = paymentManagementService.getAllPaymentsForUser(userId);
        List<Payment> convertedPayments = dtoConverter.convertTo(userPayments);
        return ResponseEntity.ok(convertedPayments);
    }

    @Override
    public ResponseEntity<Void> updatePaymentsByUserId(Long userId,
                                                        List<Payment> payments) {
        List<PaymentModel> userPayments = dtoConverter.convertFrom(payments);
        paymentManagementService.updatePaymentsForUser(userId, userPayments);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteAllUserPayments(Long userId) {
        paymentManagementService.deleteAllPaymentsForUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
