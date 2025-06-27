package com.vincenzo.order.application.port.`in`

import com.vincenzo.order.domain.model.Order
import java.math.BigDecimal

/**
 * 주문 관련 Use Case 인터페이스
 */
interface OrderUseCase {
    /**
     * 주문 생성
     */
    fun createOrder(request: CreateOrderRequest): Order
    
    /**
     * 주문 조회
     */
    fun getOrder(orderId: Long): Order?
    
    /**
     * 주문 번호로 조회
     */
    fun getOrderByOrderNumber(orderNumber: String): Order?
    
    /**
     * 회원의 주문 목록 조회
     */
    fun getOrdersByMember(memberId: Long): List<Order>
    
    /**
     * 주문 확정
     */
    fun confirmOrder(orderId: Long): Order
    
    /**
     * 주문 취소
     */
    fun cancelOrder(orderId: Long, reason: String?): Order
    
    /**
     * 주문 완료
     */
    fun completeOrder(orderId: Long): Order
}

/**
 * 주문 생성 요청
 */
data class CreateOrderRequest(
    val memberId: Long,
    val orderItems: List<OrderItemRequest>,
    val paymentInfo: PaymentInfoRequest
)

/**
 * 주문 상품 요청
 */
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

/**
 * 결제 정보 요청
 */
data class PaymentInfoRequest(
    val paymentType: String, // SINGLE, COMBINED
    val paymentMethods: List<PaymentMethodRequest>
)

/**
 * 결제 방법 요청
 */
data class PaymentMethodRequest(
    val methodType: String, // PG, CASHPOINT, COUPON, BNPL
    val amount: BigDecimal,
    val additionalInfo: Map<String, String> = emptyMap()
)
