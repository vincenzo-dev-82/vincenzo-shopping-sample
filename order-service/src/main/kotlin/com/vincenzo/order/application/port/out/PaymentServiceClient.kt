package com.vincenzo.order.application.port.out

import java.math.BigDecimal

/**
 * 결제 서비스 클라이언트 인터페이스
 */
interface PaymentServiceClient {
    /**
     * 결제 처리
     */
    suspend fun processPayment(request: PaymentRequest): PaymentResult
}

/**
 * 결제 요청
 */
data class PaymentRequest(
    val paymentKey: String,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: BigDecimal,
    val paymentType: String,
    val paymentMethods: List<PaymentMethodInfo>
)

/**
 * 결제 방법 정보
 */
data class PaymentMethodInfo(
    val methodType: String,
    val amount: BigDecimal,
    val additionalInfo: Map<String, String>
)

/**
 * 결제 결과
 */
data class PaymentResult(
    val success: Boolean,
    val message: String,
    val paymentKey: String? = null,
    val status: String? = null
)
