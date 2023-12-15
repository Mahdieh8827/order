package com.payment.repository;

import com.payment.dto.PaymentStatisticDto;
import com.payment.entity.Payment;
import com.payment.entity.enums.PaymentStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p " +
            "WHERE p.customerName LIKE %:customerName% ")
    List<Payment> findByCustomerName(String customerName, Sort sort);

    boolean existsByReferenceId(Long referenceId);
    long countByCreditCardNumberAndStatus(String creditCardNumber, PaymentStatus status);

    @Query("SELECT NEW com.payment.dto.PaymentStatisticDto(m.name, COUNT(p.id), SUM(p.amount), SUM(p.fee)) " +
            "FROM Payment p JOIN p.merchant m " +
            "WHERE p.status = 'PAYMENT_SUCCESS' OR p.status = 'REFUND' " +
            "GROUP BY m.id")
    List<PaymentStatisticDto> getPaymentStatistics();

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN Payment r ON p.id = r.referenceId " +
            "WHERE p.id = :paymentId OR r.id = :paymentId OR p.referenceId = :paymentId")
    List<Payment> findPaymentAndRefundById(@Param("paymentId") Long paymentId);
}
