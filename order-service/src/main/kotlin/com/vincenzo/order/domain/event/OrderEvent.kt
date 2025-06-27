package com.vincenzo.order.domain.event

import com.vincenzo.order.domain.model.Order
import java.time.LocalDateTime

/**
 * 주문 이벤트
 */
sealed class OrderEvent {
    abstract val orderId: Long
    abstract val orderNumber: String
    abstract val timestamp: LocalDateTime
}

/**
 * 주문 생성 이벤트
 */
data class OrderCreatedEvent(
    override val orderId: Long,
    override val orderNumber: String,
    val memberId: Long,
    val totalAmount: java.math.BigDecimal,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : OrderEvent()

/**
 * 주문 확정 이벤트
 */
data class OrderConfirmedEvent(
    override val orderId: Long,
    override val orderNumber: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : OrderEvent()

/**
 * 주문 취소 이벤트
 */
data class OrderCancelledEvent(
    override val orderId: Long,
    override val orderNumber: String,
    val reason: String? = null,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : OrderEvent()

/**
 * 주문 완료 이벤트
 */
data class OrderCompletedEvent(
    override val orderId: Long,
    override val orderNumber: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : OrderEvent()
