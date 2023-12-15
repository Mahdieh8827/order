package com.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentStatisticDto {
    private String merchantName;
    private long totalPayment;
    private double totalAmount;
    private double totalFee;
}
