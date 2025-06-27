package com.vincenzo.payment.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 도메인 모델
 */
data class Payment(
    val id: Long? = null,
    val paymentKey: String,
    val orderId: Long,
    val totalAmount: BigDecimal,
    val paymentType: PaymentType,
    val paymentMethods: List<PaymentMethod>,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(paymentMethods.isNotEmpty()) { "결제 방법이 비어있습니다." }
        require(totalAmount > BigDecimal.ZERO) { "결제 금액은 0보다 커야 합니다." }
        
        val methodTotal = paymentMethods.sumOf { it.amount }
        require(methodTotal == totalAmount) { "결제 방법별 금액의 합이 총 결제 금액과 일치하지 않습니다." }
        
        validatePaymentRules()
    }
    
    /**
     * 결제 완료
     */
    fun complete(): Payment {
        require(status == PaymentStatus.PENDING) { "대기 중인 결제만 완료할 수 있습니다." }
        return copy(status = PaymentStatus.COMPLETED)
    }
    
    /**
     * 결제 실패
     */
    fun fail(): Payment {
        require(status == PaymentStatus.PENDING) { "대기 중인 결제만 실패로 변경할 수 있습니다." }
        return copy(status = PaymentStatus.FAILED)
    }
    
    /**
     * 결제 취소
     */
    fun cancel(): Payment {
        require(status in listOf(PaymentStatus.PENDING, PaymentStatus.COMPLETED)) {
            "대기 중이거나 완료된 결제만 취소할 수 있습니다."
        }
        return copy(status = PaymentStatus.CANCELLED)
    }
    
    /**
     * 결제 규칙 검증
     */
    private fun validatePaymentRules() {
        when (paymentType) {
            PaymentType.SINGLE -> {
                require(paymentMethods.size == 1) { "단일 결제는 하나의 결제 방법만 사용할 수 있습니다." }
                
                val method = paymentMethods.first()
                when (method.methodType) {
                    PaymentMethodType.COUPON -> {
                        throw IllegalArgumentException("쿠폰은 단독 결제가 불가능합니다.")
                    }
                    else -> { /* 다른 방법들은 단독 결제 가능 */ }
                }
            }
            PaymentType.COMBINED -> {
                require(paymentMethods.size > 1) { "복합 결제는 두 개 이상의 결제 방법을 사용해야 합니다." }
                
                val hasPG = paymentMethods.any { it.methodType == PaymentMethodType.PG }
                require(hasPG) { "복합 결제에는 PG 결제가 포함되어야 합니다." }
                
                val hasBNPL = paymentMethods.any { it.methodType == PaymentMethodType.BNPL }
                require(!hasBNPL) { "BNPL은 단독 결제만 가능합니다." }
            }
        }
    }
}

/**
 * 결제 방법
 */
data class PaymentMethod(
    val id: Long? = null,
    val methodType: PaymentMethodType,
    val amount: BigDecimal,
    val status: PaymentMethodStatus = PaymentMethodStatus.PENDING,
    val externalTransactionId: String? = null,
    val additionalInfo: Map<String, String> = emptyMap()
) {
    init {
        require(amount > BigDecimal.ZERO) { "결제 금액은 0보다 커야 합니다." }
    }
    
    fun complete(externalTransactionId: String? = null): PaymentMethod {
        return copy(
            status = PaymentMethodStatus.COMPLETED,
            externalTransactionId = externalTransactionId
        )
    }
    
    fun fail(): PaymentMethod {
        return copy(status = PaymentMethodStatus.FAILED)
    }
}

/**
 * 결제 타입
 */
enum class PaymentType {
    SINGLE,     // 단일 결제
    COMBINED    // 복합 결제
}

/**
 * 결제 방법 타입
 */
enum class PaymentMethodType {
    PG,         // PG 결제
    CASHPOINT,  // 캐시포인트
    COUPON,     // 쿠폰
    BNPL        // 외상결제 (Buy Now Pay Later)
}

/**
 * 결제 상태
 */
enum class PaymentStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED,     // 실패
    CANCELLED   // 취소
}

/**
 * 결제 방법 상태
 */
enum class PaymentMethodStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED      // 실패
}
