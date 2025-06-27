package com.vincenzo.payment.adapter.`in`.web

import com.vincenzo.payment.application.port.`in`.PaymentUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "결제 관리 API")
class PaymentController(
    private val paymentUseCase: PaymentUseCase
) {
    
    @PostMapping
    @Operation(summary = "결제 처리", description = "결제를 처리합니다.")
    fun processPayment(@RequestBody request: ProcessPaymentWebRequest): ResponseEntity<PaymentResponse> {
        return runBlocking {
            try {
                val result = paymentUseCase.processPayment(request.toUseCaseRequest())
                
                if (result.success) {
                    ResponseEntity.ok(
                        PaymentResponse.success(
                            result.message,
                            result.payment?.let { PaymentDto.fromDomain(it) }
                        )
                    )
                } else {
                    ResponseEntity.badRequest()
                        .body(PaymentResponse.failure(result.message))
                }
            } catch (e: Exception) {
                ResponseEntity.internalServerError()
                    .body(PaymentResponse.failure(e.message ?: "서버 오류가 발생했습니다."))
            }
        }
    }
    
    @PostMapping("/{paymentKey}/cancel")
    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    fun cancelPayment(
        @PathVariable paymentKey: String,
        @RequestBody request: CancelPaymentWebRequest
    ): ResponseEntity<PaymentResponse> {
        return runBlocking {
            try {
                val payment = paymentUseCase.cancelPayment(paymentKey, request.reason)
                ResponseEntity.ok(
                    PaymentResponse.success(
                        "결제가 성공적으로 취소되었습니다.",
                        PaymentDto.fromDomain(payment)
                    )
                )
            } catch (e: Exception) {
                ResponseEntity.badRequest()
                    .body(PaymentResponse.failure(e.message ?: "결제 취소 실패"))
            }
        }
    }
    
    @GetMapping("/{paymentKey}/status")
    @Operation(summary = "결제 상태 조회", description = "결제 상태를 조회합니다.")
    fun getPaymentStatus(@PathVariable paymentKey: String): ResponseEntity<PaymentResponse> {
        val payment = paymentUseCase.getPaymentStatus(paymentKey)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(
            PaymentResponse.success(
                "성공",
                PaymentDto.fromDomain(payment)
            )
        )
    }
}
