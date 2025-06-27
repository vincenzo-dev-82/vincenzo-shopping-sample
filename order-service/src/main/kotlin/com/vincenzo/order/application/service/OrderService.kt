package com.vincenzo.order.application.service

import com.vincenzo.order.application.port.`in`.*
import com.vincenzo.order.application.port.out.*
import com.vincenzo.order.domain.event.*
import com.vincenzo.order.domain.model.Order
import com.vincenzo.order.domain.model.OrderItem
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val memberServiceClient: MemberServiceClient,
    private val productServiceClient: ProductServiceClient,
    private val paymentServiceClient: PaymentServiceClient,
    private val eventPublisher: EventPublisher
) : OrderUseCase {
    
    override fun createOrder(request: CreateOrderRequest): Order {
        return runBlocking {
            // 1. 회원 검증
            val member = memberServiceClient.getMember(request.memberId)
                ?: throw IllegalArgumentException("존재하지 않는 회원입니다.")
            
            if (member.status != "ACTIVE") {
                throw IllegalStateException("활성 상태의 회원만 주문할 수 있습니다.")
            }
            
            // 2. 상품 정보 조회 및 재고 확인
            val productIds = request.orderItems.map { it.productId }
            val products = productServiceClient.getProducts(productIds)
            
            if (products.size != request.orderItems.size) {
                throw IllegalArgumentException("존재하지 않는 상품이 있습니다.")
            }
            
            // 재고 확인
            for (orderItem in request.orderItems) {
                val available = productServiceClient.checkStock(orderItem.productId, orderItem.quantity)
                if (!available) {
                    val product = products.find { it.id == orderItem.productId }
                    throw IllegalStateException("상품 '${product?.name}'의 재고가 부족합니다.")
                }
            }
            
            // 3. 주문 아이템 생성 및 금액 계산
            val orderItems = request.orderItems.map { orderItemRequest ->
                val product = products.find { it.id == orderItemRequest.productId }!!
                OrderItem(
                    productId = product.id,
                    quantity = orderItemRequest.quantity,
                    unitPrice = product.price
                )
            }
            
            val totalAmount = orderItems.sumOf { it.totalPrice }
            
            // 4. 결제 금액 검증
            val paymentTotalAmount = request.paymentInfo.paymentMethods.sumOf { it.amount }
            if (paymentTotalAmount != totalAmount) {
                throw IllegalArgumentException("결제 금액과 주문 금액이 일치하지 않습니다.")
            }
            
            // 5. 결제 방법 유효성 검증
            validatePaymentMethods(request.paymentInfo, member.cashpointBalance)
            
            // 6. 주문 생성
            val order = Order(
                memberId = request.memberId,
                orderItems = orderItems,
                totalAmount = totalAmount
            )
            
            val savedOrder = orderRepository.save(order)
            
            // 7. 결제 처리
            val paymentKey = "PAY_${savedOrder.orderNumber}_${System.currentTimeMillis()}"
            val paymentRequest = PaymentRequest(
                paymentKey = paymentKey,
                orderId = savedOrder.id!!,
                memberId = savedOrder.memberId,
                totalAmount = savedOrder.totalAmount,
                paymentType = request.paymentInfo.paymentType,
                paymentMethods = request.paymentInfo.paymentMethods.map {
                    PaymentMethodInfo(
                        methodType = it.methodType,
                        amount = it.amount,
                        additionalInfo = it.additionalInfo
                    )
                }
            )
            
            val paymentResult = paymentServiceClient.processPayment(paymentRequest)
            
            if (!paymentResult.success) {
                // 결제 실패 시 주문 취소
                val cancelledOrder = savedOrder.cancel()
                orderRepository.save(cancelledOrder)
                
                val cancelEvent = OrderCancelledEvent(
                    orderId = cancelledOrder.id!!,
                    orderNumber = cancelledOrder.orderNumber,
                    reason = "결제 실패: ${paymentResult.message}"
                )
                eventPublisher.publishOrderEvent(cancelEvent)
                
                throw IllegalStateException("결제가 실패했습니다: ${paymentResult.message}")
            }
            
            // 8. 주문 확정
            val confirmedOrder = savedOrder.confirm()
            val finalOrder = orderRepository.save(confirmedOrder)
            
            // 9. 이벤트 발행
            val createEvent = OrderCreatedEvent(
                orderId = finalOrder.id!!,
                orderNumber = finalOrder.orderNumber,
                memberId = finalOrder.memberId,
                totalAmount = finalOrder.totalAmount
            )
            eventPublisher.publishOrderEvent(createEvent)
            
            val confirmEvent = OrderConfirmedEvent(
                orderId = finalOrder.id!!,
                orderNumber = finalOrder.orderNumber
            )
            eventPublisher.publishOrderEvent(confirmEvent)
            
            finalOrder
        }
    }
    
    @Transactional(readOnly = true)
    override fun getOrder(orderId: Long): Order? {
        return orderRepository.findById(orderId)
    }
    
    @Transactional(readOnly = true)
    override fun getOrderByOrderNumber(orderNumber: String): Order? {
        return orderRepository.findByOrderNumber(orderNumber)
    }
    
    @Transactional(readOnly = true)
    override fun getOrdersByMember(memberId: Long): List<Order> {
        return orderRepository.findByMemberId(memberId)
    }
    
    override fun confirmOrder(orderId: Long): Order {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다.")
        
        val confirmedOrder = order.confirm()
        val savedOrder = orderRepository.save(confirmedOrder)
        
        val event = OrderConfirmedEvent(
            orderId = savedOrder.id!!,
            orderNumber = savedOrder.orderNumber
        )
        eventPublisher.publishOrderEvent(event)
        
        return savedOrder
    }
    
    override fun cancelOrder(orderId: Long, reason: String?): Order {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다.")
        
        val cancelledOrder = order.cancel()
        val savedOrder = orderRepository.save(cancelledOrder)
        
        val event = OrderCancelledEvent(
            orderId = savedOrder.id!!,
            orderNumber = savedOrder.orderNumber,
            reason = reason
        )
        eventPublisher.publishOrderEvent(event)
        
        return savedOrder
    }
    
    override fun completeOrder(orderId: Long): Order {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다.")
        
        val completedOrder = order.complete()
        val savedOrder = orderRepository.save(completedOrder)
        
        val event = OrderCompletedEvent(
            orderId = savedOrder.id!!,
            orderNumber = savedOrder.orderNumber
        )
        eventPublisher.publishOrderEvent(event)
        
        return savedOrder
    }
    
    /**
     * 결제 방법 유효성 검증
     */
    private fun validatePaymentMethods(paymentInfo: PaymentInfoRequest, cashpointBalance: BigDecimal) {
        val methods = paymentInfo.paymentMethods
        
        when (paymentInfo.paymentType) {
            "SINGLE" -> {
                require(methods.size == 1) { "단일 결제는 하나의 결제수단만 사용할 수 있습니다." }
                
                val method = methods.first()
                when (method.methodType) {
                    "COUPON" -> throw IllegalArgumentException("쿠폰은 단독 결제가 불가능합니다.")
                    "CASHPOINT" -> {
                        if (cashpointBalance < method.amount) {
                            throw IllegalStateException("캐시포인트 잔액이 부족합니다.")
                        }
                    }
                }
            }
            "COMBINED" -> {
                require(methods.size > 1) { "복합 결제는 두 개 이상의 결제수단을 사용해야 합니다." }
                
                val hasPG = methods.any { it.methodType == "PG" }
                require(hasPG) { "복합 결제에는 PG 결제가 포함되어야 합니다." }
                
                val hasBNPL = methods.any { it.methodType == "BNPL" }
                require(!hasBNPL) { "BNPL은 단독 결제만 가능합니다." }
                
                val cashpointAmount = methods.filter { it.methodType == "CASHPOINT" }.sumOf { it.amount }
                if (cashpointAmount > BigDecimal.ZERO && cashpointBalance < cashpointAmount) {
                    throw IllegalStateException("캐시포인트 잔액이 부족합니다.")
                }
            }
            else -> throw IllegalArgumentException("지원하지 않는 결제 타입입니다: ${paymentInfo.paymentType}")
        }
    }
}
