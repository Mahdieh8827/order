package com.payment.dto;

import com.payment.entity.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {
    private Long paymentId;
    private double amount;
    private String customerName;
    private String creditCardNumber;
    private String expiryDate;
    private Long merchantId;
    private PaymentStatus status;
}
