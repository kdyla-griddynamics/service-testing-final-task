package com.griddynamics.gridu.qa.payment.service;

import com.griddynamics.gridu.qa.gateway.api.CardApi;
import com.griddynamics.gridu.qa.payment.db.dao.PaymentRepository;
import com.griddynamics.gridu.qa.payment.db.model.PaymentModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class PaymentManagementService {

    private final PaymentRepository paymentRepository;
    private final CardApi cardApi;
    private final DtoConverter dtoConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentManagementService.class);

    public PaymentModel saveOrUpdatePayment(PaymentModel paymentModel) {
        return callPaymentServiceAndSave(paymentModel);
    }

    public List<PaymentModel> getAllPaymentsForUser(Long userId) {
        return paymentRepository.findPaymentModelByUserId(userId);
    }

    @Transactional
    public void updatePaymentsForUser(Long userId, List<PaymentModel> newPayments) {
        /*  Unlike Address API we can not just remove all payments before adding new (since there's the token generated for them)
            so is better to proceed all payments independently.
        */
        List<PaymentModel> currentPayments = paymentRepository.findPaymentModelByUserId(userId);
        List<PaymentModel> paymentsToRemove
                = currentPayments.stream()
                                 .filter(existing -> newPayments.stream()
                                                                 .anyMatch(paymentModel -> Objects.equals(existing.getId(), paymentModel.getId())))
                                 .collect(Collectors.toList());
        // remove all payments which are not present in the current list
        paymentRepository.deleteAll(paymentsToRemove);
        // all existing payments (having id) should be updated (or not touched if nothing has changed)
        newPayments.forEach(paymentModel -> saveIfNewOrChanged(paymentModel, currentPayments));
    }

    @Transactional
    public void deleteAllPaymentsForUser(Long userId) {
        paymentRepository.deletePaymentModelByUserId(userId);
    }

    private void saveIfNewOrChanged(PaymentModel objectToSaveOrUpdate, List<PaymentModel> existing) {
        // not yet saved
        if (isEmpty(objectToSaveOrUpdate)
            // changed or not verified before
            || !(objectToSaveOrUpdate.isTokenFilled() && existing.contains(objectToSaveOrUpdate)))
            callPaymentServiceAndSave(objectToSaveOrUpdate);
    }

    private PaymentModel callPaymentServiceAndSave(PaymentModel payment) {
        callPaymentGateway(payment);
        return paymentRepository.save(payment);
    }

    private void callPaymentGateway(PaymentModel payment) {
        try {
            String token = cardApi.verifyCard(dtoConverter.convertToCard(payment));
            payment.setToken(token);
        } catch (Exception exception) { // not the best pattern (it's better to handle certain types)
            LOGGER.warn("Exception while sending payment to the gateway: ", exception);
            payment.setToken(PaymentModel.FAILED_TOKEN);
        }
    }

}
