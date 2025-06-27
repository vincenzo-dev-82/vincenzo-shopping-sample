package com.vincenzo.order.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * 주문 도메인 모델
 */
data class Order(
    val id: Long? = null,
    val orderNumber: String = generateOrderNumber(),
    val memberId: Long,
    val orderItems: List<OrderItem>,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val finalAmount: BigDecimal = totalAmount - discountAmount,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(orderItems.isNotEmpty()) { "주문 상품이 비어있습니다." }
        require(totalAmount > BigDecimal.ZERO) { "주문 금액은 0보다 커야 합니다." }
        require(finalAmount >= BigDecimal.ZERO) { "최종 결제 금액은 0 이상이어야 합니다." }
    }
    
    /**
     * 주문 확정
     */
    fun confirm(): Order {
        require(status == OrderStatus.PENDING) { "대기 중인 주문만 확정할 수 있습니다." }
        return copy(status = OrderStatus.CONFIRMED)
    }
    
    /**
     * 주문 취소
     */
    fun cancel(): Order {
        require(status in listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)) {
            "대기 중이거나 확정된 주문만 취소할 수 있습니다."
        }
        return copy(status = OrderStatus.CANCELLED)
    }
    
    /**
     * 주문 완료
     */
    fun complete(): Order {
        require(status == OrderStatus.CONFIRMED) { "확정된 주문만 완료할 수 있습니다." }
        return copy(status = OrderStatus.COMPLETED)
    }
    
    companion object {
        private fun generateOrderNumber(): String {
            val timestamp = System.currentTimeMillis()
            val random = Random().nextInt(1000).toString().padStart(3, '0')
            return "ORD${timestamp}${random}"
        }
    }
}

/**
 * 주문 상품
 */
data class OrderItem(
    val id: Long? = null,
    val productId: Long,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal = unitPrice * BigDecimal.valueOf(quantity.toLong())
) {
    init {
        require(quantity > 0) { "주문 수량은 0보다 커야 합니다." }
        require(unitPrice > BigDecimal.ZERO) { "상품 가격은 0보다 커야 합니다." }
    }
}

/**
 * 주문 상태
 */
enum class OrderStatus {
    PENDING,    // 대기
    CONFIRMED,  // 확정
    COMPLETED,  // 완료
    CANCELLED   // 취소
}
