package com.vincenzo.payment.application.port.out

import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType

/**
 * 결제 처리기 인터페이스 (Strategy Pattern)
 */
interface PaymentProcessor {
    /**
     * 지원하는 결제 방법 타입
     */
    fun supports(methodType: PaymentMethodType): Boolean
    
    /**
     * 결제 처리
     */
    suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult
    
    /**
     * 결제 취소/환불
     */
    suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult
}

/**
 * 결제 처리 결과
 */
data class PaymentProcessResult(
    val success: Boolean,
    val message: String,
    val externalTransactionId: String? = null
)
