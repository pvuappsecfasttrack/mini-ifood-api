package com.marcosdias.miniifood.payment.web;

import com.marcosdias.miniifood.payment.service.MockPaymentService;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentRequest;
import com.marcosdias.miniifood.payment.web.dto.MockPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Mock payment endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final MockPaymentService mockPaymentService;

    @PostMapping("/mock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Process mock payment", description = "Processes a mocked payment without external gateway integration")
    public ResponseEntity<MockPaymentResponse> processMockPayment(@Valid @RequestBody MockPaymentRequest request) {
        log.info("Received mock payment request for order {}", request.orderId());
        return ResponseEntity.ok(mockPaymentService.process(request));
    }
}

