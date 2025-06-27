package com.vincenzo.payment.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 이벤트
 */
sealed class PaymentEvent {
    abstract val paymentKey: String
    abstract val orderId: Long
    abstract val amount: BigDecimal
    abstract val timestamp: LocalDateTime
}

/**
 * 결제 완료 이벤트
 */
data class PaymentCompletedEvent(
    override val paymentKey: String,
    override val orderId: Long,
    override val amount: BigDecimal,
    val memberId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : PaymentEvent()

/**
 * 결제 실패 이벤트
 */
data class PaymentFailedEvent(
    override val paymentKey: String,
    override val orderId: Long,
    override val amount: BigDecimal,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : PaymentEvent()

/**
 * 결제 취소 이벤트
 */
data class PaymentCancelledEvent(
    override val paymentKey: String,
    override val orderId: Long,
    override val amount: BigDecimal,
    val reason: String? = null,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : PaymentEvent()
