package com.payment.dto;

import com.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private PaymentStatus status;
}
