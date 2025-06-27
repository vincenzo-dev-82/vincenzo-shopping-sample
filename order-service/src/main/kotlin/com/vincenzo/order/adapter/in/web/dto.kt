package com.vincenzo.order.adapter.`in`.web

import com.vincenzo.order.application.port.`in`.*
import com.vincenzo.order.domain.model.Order
import com.vincenzo.order.domain.model.OrderItem
import com.vincenzo.order.domain.model.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 생성 웹 요청 DTO
 */
data class CreateOrderWebRequest(
    val memberId: Long,
    val orderItems: List<OrderItemWebRequest>,
    val paymentInfo: PaymentInfoWebRequest
) {
    fun toUseCaseRequest(): CreateOrderRequest {
        return CreateOrderRequest(
            memberId = memberId,
            orderItems = orderItems.map { it.toUseCaseRequest() },
            paymentInfo = paymentInfo.toUseCaseRequest()
        )
    }
}

/**
 * 주문 상품 웹 요청 DTO
 */
data class OrderItemWebRequest(
    val productId: Long,
    val quantity: Int
) {
    fun toUseCaseRequest(): OrderItemRequest {
        return OrderItemRequest(
            productId = productId,
            quantity = quantity
        )
    }
}

/**
 * 결제 정보 웹 요청 DTO
 */
data class PaymentInfoWebRequest(
    val paymentType: String,
    val paymentMethods: List<PaymentMethodWebRequest>
) {
    fun toUseCaseRequest(): PaymentInfoRequest {
        return PaymentInfoRequest(
            paymentType = paymentType,
            paymentMethods = paymentMethods.map { it.toUseCaseRequest() }
        )
    }
}

/**
 * 결제 방법 웹 요청 DTO
 */
data class PaymentMethodWebRequest(
    val methodType: String,
    val amount: BigDecimal,
    val additionalInfo: Map<String, String> = emptyMap()
) {
    fun toUseCaseRequest(): PaymentMethodRequest {
        return PaymentMethodRequest(
            methodType = methodType,
            amount = amount,
            additionalInfo = additionalInfo
        )
    }
}

/**
 * 주문 취소 요청 DTO
 */
data class CancelOrderRequest(
    val reason: String?
)

/**
 * 주문 응답 DTO
 */
data class OrderResponse(
    val success: Boolean = true,
    val message: String? = null,
    val order: OrderDto? = null
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse {
            return OrderResponse(
                success = true,
                order = OrderDto.fromDomain(order)
            )
        }
        
        fun error(message: String): OrderResponse {
            return OrderResponse(
                success = false,
                message = message
            )
        }
    }
}

/**
 * 주문 DTO
 */
data class OrderDto(
    val id: Long,
    val orderNumber: String,
    val memberId: Long,
    val orderItems: List<OrderItemDto>,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val status: OrderStatus,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(order: Order): OrderDto {
            return OrderDto(
                id = order.id!!,
                orderNumber = order.orderNumber,
                memberId = order.memberId,
                orderItems = order.orderItems.map { OrderItemDto.fromDomain(it) },
                totalAmount = order.totalAmount,
                discountAmount = order.discountAmount,
                finalAmount = order.finalAmount,
                status = order.status,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}

/**
 * 주문 상품 DTO
 */
data class OrderItemDto(
    val id: Long?,
    val productId: Long,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
) {
    companion object {
        fun fromDomain(orderItem: OrderItem): OrderItemDto {
            return OrderItemDto(
                id = orderItem.id,
                productId = orderItem.productId,
                quantity = orderItem.quantity,
                unitPrice = orderItem.unitPrice,
                totalPrice = orderItem.totalPrice
            )
        }
    }
}
