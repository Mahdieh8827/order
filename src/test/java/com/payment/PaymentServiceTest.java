package com.payment;

import com.payment.dto.PaymentDto;
import com.payment.dto.PaymentResponseDto;
import com.payment.dto.PaymentStatisticDto;
import com.payment.entity.Merchant;
import com.payment.entity.Payment;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.AccessDeniedMerchantException;
import com.payment.exception.InvalidPaymentException;
import com.payment.repository.MerchantRepository;
import com.payment.repository.PaymentRepository;
import com.payment.service.PaymentService;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Value("${payment.fee.successful}") double paymentFeeSuccessful;
    @Value("${payment.fee.first.failed}") double paymentFeeFirstFailed;
    @Value("${payment.fee.subsequent.failed}") double paymentFeeSubsequentFailed;
    @Value("${refund.fee}") double refundFee;
    private PaymentService paymentService ;

    @BeforeEach
    void initializePaymentService()
    {
         paymentService = new PaymentService(paymentRepository, merchantRepository, paymentFeeSuccessful, paymentFeeFirstFailed, paymentFeeSubsequentFailed, refundFee);
    }
    @Test
    void testProcessPayment_Successful()
    //TODO testName
    {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setCreditCardNumber("1234567890123456");
        paymentDto.setMerchantId(1001L);

        Merchant merchant = new Merchant(1001L,"Amazon");

        Payment mockPayment = new Payment();
        mockPayment.setId(1L);
        mockPayment.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        mockPayment.setMerchant(merchant);

        when(merchantRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(merchant));
        Mockito.when(paymentRepository.save(ArgumentMatchers.any(Payment.class))).thenReturn(mockPayment);

        PaymentResponseDto responseDto = paymentService.processPayment(paymentDto);
        assertEquals(1L, responseDto.getPaymentId());
        assertEquals(PaymentStatus.PAYMENT_SUCCESS, responseDto.getStatus());
    }

    @Test
    void testProcessPayment_Failed() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setCreditCardNumber("5123456789012345");
        paymentDto.setMerchantId(1001L);

        Merchant merchant = new Merchant(1001L,"Amazon");

        Payment mockPayment = new Payment();
        mockPayment.setId(2L);
        mockPayment.setStatus(PaymentStatus.PAYMENT_FAILED);

        when(merchantRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(merchant));
        when(paymentRepository.save(ArgumentMatchers.any(Payment.class))).thenReturn(mockPayment);
        when(paymentRepository.countByCreditCardNumberAndStatus(ArgumentMatchers.any(String.class), ArgumentMatchers.any(PaymentStatus.class))).thenReturn(2L);

        PaymentResponseDto responseDto = paymentService.processPayment(paymentDto);

        assertEquals(2L, responseDto.getPaymentId());
        assertEquals(PaymentStatus.PAYMENT_FAILED, responseDto.getStatus());
    }
    @Test
    void testRefundPayment_Successful() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(requestMerchantId,"Amazon"));
        existingPayment.setStatus(PaymentStatus.PAYMENT_SUCCESS);

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        Mockito.when(merchantRepository.getReferenceById(requestMerchantId)).thenReturn(new Merchant(requestMerchantId,"Amazon"));
        Mockito.when(paymentRepository.save(ArgumentMatchers.any(Payment.class))).thenReturn(new Payment());

        Assertions.assertDoesNotThrow(() -> paymentService.refundPayment(paymentId, requestMerchantId));

        Mockito.verify(paymentRepository, Mockito.times(1)).findById(paymentId);
        Mockito.verify(merchantRepository, Mockito.times(1)).getReferenceById(requestMerchantId);
        Mockito.verify(paymentRepository, Mockito.times(1)).save(ArgumentMatchers.any(Payment.class));
    }

    @Test
    void testRefundPayment_Failed() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(requestMerchantId,"Amazon"));
        existingPayment.setStatus(PaymentStatus.PAYMENT_FAILED);

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        Assertions.assertThrows(InvalidPaymentException.class, () -> paymentService.refundPayment(paymentId, requestMerchantId));
    }

    @Test
    void testReRefundPayment() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(requestMerchantId,"Amazon"));
        existingPayment.setStatus(PaymentStatus.PAYMENT_SUCCESS);

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        Mockito.when(paymentRepository.existsByReferenceId(paymentId)).thenReturn(true);

        Assertions.assertThrows(InvalidPaymentException.class, () -> paymentService.refundPayment(paymentId, requestMerchantId));
    }

    @Test
    void testRefundPayment_NotAuthorized() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(3L,"Temu"));

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        Assertions.assertThrows(AccessDeniedMerchantException.class, () -> paymentService.refundPayment(paymentId, requestMerchantId));

        Mockito.verify(paymentRepository, Mockito.never()).save(ArgumentMatchers.any(Payment.class));
    }

    @Test
    void testViewPayment_Successful() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(requestMerchantId,"Amazon"));

        Mockito.when(paymentRepository.findPaymentAndRefundById(paymentId)).thenReturn(List.of(existingPayment));

        List<PaymentDto> result = paymentService.viewPayment(paymentId, requestMerchantId);

        assertNotNull(result);
        assertEquals(paymentId, result.get(0).getPaymentId());
    }

    @Test
    void testViewPayment_NotAuthorized() {
        long paymentId = 1L;
        long requestMerchantId = 2L;

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setMerchant(new Merchant(3L,"Temu"));

        Mockito.when(paymentRepository.findPaymentAndRefundById(paymentId)).thenReturn(List.of(existingPayment));

        Assertions.assertThrows(AccessDeniedMerchantException.class, () -> paymentService.viewPayment(paymentId, requestMerchantId));
    }

    @Test
    void testListPayments_WithCustomerName() {
        String customerName = "John Doe";
        Sort sort = Sort.by("id").ascending();

        Payment payment = new Payment();
        payment.setCustomerName("John Doe");
        payment.setCreditCardNumber("1234567890123456");
        payment.setAmount(100.0);
        payment.setMerchant(new Merchant(1001L,"Amazon"));
        payment.setExpiryDate("12/23");

        Mockito.when(paymentRepository.findByCustomerName(customerName, sort)).thenReturn(List.of(payment));

        List<PaymentDto> result = paymentService.listPayments(customerName, sort);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }
    @Test
    void testListPayments_WithoutCustomerName() {
        String customerName = "John Doe";
        Sort sort = Sort.by("id").ascending();

        Payment payment = new Payment();
        payment.setCustomerName("John Doe");
        payment.setCreditCardNumber("1234567890123456");
        payment.setAmount(100.0);
        payment.setMerchant(new Merchant(1001L,"Amazon"));
        payment.setExpiryDate("12/23");

        Mockito.when(paymentRepository.findAll(sort)).thenReturn(List.of(payment));
        List<PaymentDto> result = paymentService.listPayments(null, sort);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testGetPaymentStatistics() {
        when(paymentRepository.getPaymentStatistics()).thenReturn(List.of(new PaymentStatisticDto("Amazon",1,100,0.10)));

        List<PaymentStatisticDto> result = paymentService.getPaymentStatistics();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
