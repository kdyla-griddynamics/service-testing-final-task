package com.griddynamics.gridu.qa.payment.service;

import com.griddynamics.gridu.qa.gateway.api.model.Card;
import com.griddynamics.gridu.qa.payment.api.model.Payment;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoConverter {

    public Payment convertTo(PaymentModel paymentModel) {
        return new Payment()
                       .id(paymentModel.getId())
                       .userId(paymentModel.getUserId())
                       .cardNumber(paymentModel.getCardNumber())
                       .cardHolder(paymentModel.getCardholder())
                       .expiryYear(paymentModel.getExpiryYear())
                       .expiryMonth(paymentModel.getExpiryMonth())
                       .cvv(paymentModel.getCvv())
                       .verified(paymentModel.isTokenFilled());
    }

    public List<Payment> convertTo(List<PaymentModel> paymentModelList) {
        return paymentModelList.stream()
                               .map(this::convertTo)
                               .collect(Collectors.toList());
    }

    public PaymentModel convertFrom(Payment payment) {
        return PaymentModel.builder()
                           .id(payment.getId())
                           .userId(payment.getUserId())
                           .cardNumber(payment.getCardNumber())
                           .cardholder(payment.getCardHolder())
                           .expiryYear(payment.getExpiryYear())
                           .expiryMonth(payment.getExpiryMonth())
                           .cvv(payment.getCvv())
                           .build();
    }

    public List<PaymentModel> convertFrom(List<Payment> paymentList) {
        return paymentList.stream()
                          .map(this::convertFrom)
                          .collect(Collectors.toList());
    }

    public Card convertToCard(PaymentModel paymentModel) {
        return new Card()
                       .cardNumber(paymentModel.getCardNumber())
                       .cardHolder(paymentModel.getCardholder())
                       .expiryYear(paymentModel.getExpiryYear())
                       .expiryMonth(paymentModel.getExpiryMonth())
                       .cvv(paymentModel.getCvv());
    }
}
