package com.vincenzo.payment.application.port.`in`

import com.vincenzo.payment.domain.model.Payment
import com.vincenzo.payment.domain.model.PaymentMethodType
import com.vincenzo.payment.domain.model.PaymentType
import java.math.BigDecimal

/**
 * 결제 관련 Use Case 인터페이스
 */
interface PaymentUseCase {
    /**
     * 결제 처리
     */
    suspend fun processPayment(request: ProcessPaymentRequest): ProcessPaymentResult
    
    /**
     * 결제 취소
     */
    suspend fun cancelPayment(paymentKey: String, reason: String?): Payment
    
    /**
     * 결제 상태 조회
     */
    fun getPaymentStatus(paymentKey: String): Payment?
}

/**
 * 결제 처리 요청
 */
data class ProcessPaymentRequest(
    val paymentKey: String,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: BigDecimal,
    val paymentType: PaymentType,
    val paymentMethods: List<PaymentMethodRequest>
)

/**
 * 결제 방법 요청
 */
data class PaymentMethodRequest(
    val methodType: PaymentMethodType,
    val amount: BigDecimal,
    val additionalInfo: Map<String, String> = emptyMap()
)

/**
 * 결제 처리 결과
 */
data class ProcessPaymentResult(
    val success: Boolean,
    val message: String,
    val payment: Payment? = null
)
