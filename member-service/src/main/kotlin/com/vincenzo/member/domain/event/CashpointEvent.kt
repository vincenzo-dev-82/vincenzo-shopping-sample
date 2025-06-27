package com.vincenzo.member.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 캐시포인트 이벤트
 */
sealed class CashpointEvent {
    abstract val memberId: Long
    abstract val amount: BigDecimal
    abstract val transactionId: String
    abstract val timestamp: LocalDateTime
}

/**
 * 캐시포인트 차감 이벤트
 */
data class CashpointDeductedEvent(
    override val memberId: Long,
    override val amount: BigDecimal,
    override val transactionId: String,
    val remainingBalance: BigDecimal,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : CashpointEvent()

/**
 * 캐시포인트 환불 이벤트
 */
data class CashpointRefundedEvent(
    override val memberId: Long,
    override val amount: BigDecimal,
    override val transactionId: String,
    val newBalance: BigDecimal,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : CashpointEvent()
