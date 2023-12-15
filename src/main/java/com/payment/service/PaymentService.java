package com.payment.service;

import com.payment.dto.PaymentDto;
import com.payment.dto.PaymentResponseDto;
import com.payment.dto.PaymentStatisticDto;
import com.payment.entity.Payment;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.AccessDeniedMerchantException;
import com.payment.exception.InvalidPaymentException;
import com.payment.exception.NotFoundException;
import com.payment.repository.MerchantRepository;
import com.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
//    @Value("${payment.fee.successful}")
    private final double paymentFeeSuccessful;
//    @Value("${payment.fee.first.failed}")
    private final double paymentFeeFirstFailed;
//    @Value("${payment.fee.subsequent.failed}")
    private final double paymentFeeSubsequentFailed;
//    @Value("${refund.fee}")
    private final double refundFee;


    public PaymentService(PaymentRepository paymentRepository, MerchantRepository merchantRepository, @Value("${payment.fee.successful}") double paymentFeeSuccessful,
                          @Value("${payment.fee.first.failed}") double paymentFeeFirstFailed, @Value("${payment.fee.subsequent.failed}") double paymentFeeSubsequentFailed, @Value("${refund.fee}") double refundFee) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.paymentFeeSuccessful = paymentFeeSuccessful;
        this.paymentFeeFirstFailed = paymentFeeFirstFailed;
        this.paymentFeeSubsequentFailed = paymentFeeSubsequentFailed;
        this.refundFee = refundFee;
    }

    private Payment convertToEntity(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setExpiryDate(paymentDto.getExpiryDate());
        payment.setCustomerName(paymentDto.getCustomerName());
        payment.setMerchant(merchantRepository.getReferenceById(paymentDto.getMerchantId()));
        payment.setCreditCardNumber(paymentDto.getCreditCardNumber());
        payment.setAmount(paymentDto.getAmount());
        return payment;
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setExpiryDate(payment.getExpiryDate());
        paymentDto.setCustomerName(payment.getCustomerName());
        paymentDto.setMerchantId(payment.getMerchant().getId());
        paymentDto.setCreditCardNumber(payment.getCreditCardNumber());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setPaymentId(payment.getId());
        paymentDto.setStatus(payment.getStatus());
        return paymentDto;
    }

    public PaymentResponseDto processPayment(PaymentDto paymentDto) {
        merchantRepository.findById(paymentDto.getMerchantId())
                .orElseThrow(() -> new NotFoundException(String.format("There is no merchant with id =%s", paymentDto.getMerchantId())));

        Payment payment = convertToEntity(paymentDto);
        if (payment.getCreditCardNumber().startsWith("5")) {
            payment.setStatus(PaymentStatus.PAYMENT_FAILED);
            double fee = paymentRepository.countByCreditCardNumberAndStatus(payment.getCreditCardNumber(), PaymentStatus.PAYMENT_FAILED) > 0
                    ? paymentFeeSubsequentFailed
                    : paymentFeeFirstFailed;
            payment.setFee(fee);
        } else {
            payment.setStatus(PaymentStatus.PAYMENT_SUCCESS);
            payment.setFee(paymentFeeSuccessful);
        }
        payment = paymentRepository.save(payment);

        return new PaymentResponseDto(payment.getId(), payment.getStatus());
    }

    public void refundPayment(Long paymentId, Long requestMerchantId) {
        var existPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException(String.format("There is no payment with ID %d", paymentId)));

        if (isAuthorized(existPayment.getMerchant().getId(), requestMerchantId)) {
            if(existPayment.getStatus() != PaymentStatus.PAYMENT_SUCCESS)
                throw new InvalidPaymentException(String.format("You cannot refund the failed payment with ID %d", paymentId));
            if(paymentRepository.existsByReferenceId(existPayment.getId()))
                throw new InvalidPaymentException(String.format("You cannot reRefund the payment with ID %d", paymentId));
            Payment refund = new Payment();
            refund.setExpiryDate(existPayment.getExpiryDate());
            refund.setCustomerName(existPayment.getCustomerName());
            refund.setMerchant(merchantRepository.getReferenceById(existPayment.getMerchant().getId()));
            refund.setCreditCardNumber(existPayment.getCreditCardNumber());
            refund.setAmount((-1) * existPayment.getAmount());
            refund.setStatus(PaymentStatus.REFUND);
            refund.setFee(refundFee);
            refund.setReferenceId(existPayment.getId());
            paymentRepository.save(refund);
        } else {
            throw new AccessDeniedMerchantException("Access to payment from another merchant is not allowed");
        }
    }

    public List<PaymentDto> viewPayment(Long paymentId, Long requestMerchantId) {
        var existPayment = paymentRepository.findPaymentAndRefundById(paymentId);
        if(existPayment.isEmpty())
                throw new NotFoundException(String.format("There is no payment with id =%s", paymentId));

        if (isAuthorized(existPayment.get(0).getMerchant().getId(), requestMerchantId))
            return existPayment.stream().map(this::convertToDto).collect(Collectors.toList());
        else {
            throw new AccessDeniedMerchantException("Access to payment from another merchant is not allowed");
        }
    }

    public List<PaymentDto> listPayments(String customerName, Sort sort) {
        if (customerName != null && !customerName.isEmpty()) {
            return paymentRepository.findByCustomerName(customerName, sort).stream().map(this::convertToDto).collect(Collectors.toList());
        } else {
            return paymentRepository.findAll(sort).stream().map(this::convertToDto).collect(Collectors.toList());
        }
    }

    public List<PaymentStatisticDto> getPaymentStatistics() {
        return paymentRepository.getPaymentStatistics();
    }

    public boolean isAuthorized(Long paymentMerchantId, Long requestMerchantId) {
        return paymentMerchantId.equals(requestMerchantId);
    }
}

