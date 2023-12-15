package com.payment.controller;

import com.payment.config.MyAuthenticationToken;
import com.payment.dto.PaymentDto;
import com.payment.dto.PaymentResponseDto;
import com.payment.dto.PaymentStatisticDto;
import com.payment.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/api/payment")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class  PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('PAYMENT_MERCHANT')")
    public ResponseEntity<PaymentResponseDto> processPayment(@RequestBody PaymentDto payment) {
        return new ResponseEntity<>(paymentService.processPayment(payment), HttpStatus.CREATED);
    }

    @PostMapping("/refund/{paymentId}")
    @PreAuthorize("hasAuthority(@environment.getProperty('role.payment'))")
    public ResponseEntity<Void> refundPayment(@PathVariable Long paymentId, Authentication token) {
        paymentService.refundPayment(paymentId,((MyAuthenticationToken) token).getMerchantId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority(@environment.getProperty('role.payment'))")
    public ResponseEntity<List<PaymentDto>> viewPayment(@PathVariable Long paymentId, Authentication token) {
        return new ResponseEntity<>(paymentService.viewPayment(paymentId,((MyAuthenticationToken) token).getMerchantId()), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@environment.getProperty('role.payment'))")
    public ResponseEntity<List<PaymentDto>> listPayments(@RequestParam(required = false) String customerName,
                                          @RequestParam(defaultValue = "creationDate") String sortField) {
        return new ResponseEntity<>(paymentService.listPayments(customerName,Sort.by(Sort.Direction.DESC, sortField)), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority(@environment.getProperty('role.payment'))")
    public ResponseEntity<List<PaymentStatisticDto>> getPaymentStatistics() {
        return new ResponseEntity<>(paymentService.getPaymentStatistics(), HttpStatus.OK);
    }
}


