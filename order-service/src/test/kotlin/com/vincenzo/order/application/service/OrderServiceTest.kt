package com.vincenzo.order.application.service

import com.vincenzo.order.application.port.`in`.*
import com.vincenzo.order.application.port.out.*
import com.vincenzo.order.domain.model.Order
import com.vincenzo.order.domain.model.OrderItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class OrderServiceTest {
    
    private val orderRepository = mockk<OrderRepository>()
    private val memberServiceClient = mockk<MemberServiceClient>()
    private val productServiceClient = mockk<ProductServiceClient>()
    private val paymentServiceClient = mockk<PaymentServiceClient>()
    private val eventPublisher = mockk<EventPublisher>(relaxed = true)
    
    private val orderService = OrderService(
        orderRepository,
        memberServiceClient,
        productServiceClient,
        paymentServiceClient,
        eventPublisher
    )
    
    @Test
    fun `주문 생성 성공`() = runBlocking {
        // Given
        val request = createOrderRequest()
        val member = createTestMember()
        val products = listOf(createTestProduct())
        val paymentResult = PaymentResult(true, "결제 성공", "PAY123", "COMPLETED")
        
        coEvery { memberServiceClient.getMember(1L) } returns member
        coEvery { productServiceClient.getProducts(listOf(1L)) } returns products
        coEvery { productServiceClient.checkStock(1L, 2) } returns true
        coEvery { paymentServiceClient.processPayment(any()) } returns paymentResult
        every { orderRepository.save(any()) } returnsArgument 0
        
        // When
        val result = orderService.createOrder(request)
        
        // Then
        assertThat(result.memberId).isEqualTo(1L)
        assertThat(result.orderItems).hasSize(1)
        assertThat(result.totalAmount).isEqualTo(BigDecimal("1798000.00"))
    }
    
    @Test
    fun `존재하지 않는 회원으로 주문 생성시 실패`() = runBlocking {
        // Given
        val request = createOrderRequest()
        coEvery { memberServiceClient.getMember(1L) } returns null
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            runBlocking { orderService.createOrder(request) }
        }
    }
    
    @Test
    fun `재고 부족시 주문 생성 실패`() = runBlocking {
        // Given
        val request = createOrderRequest()
        val member = createTestMember()
        val products = listOf(createTestProduct())
        
        coEvery { memberServiceClient.getMember(1L) } returns member
        coEvery { productServiceClient.getProducts(listOf(1L)) } returns products
        coEvery { productServiceClient.checkStock(1L, 2) } returns false
        
        // When & Then
        assertThrows<IllegalStateException> {
            runBlocking { orderService.createOrder(request) }
        }
    }
    
    private fun createOrderRequest(): CreateOrderRequest {
        return CreateOrderRequest(
            memberId = 1L,
            orderItems = listOf(
                OrderItemRequest(productId = 1L, quantity = 2)
            ),
            paymentInfo = PaymentInfoRequest(
                paymentType = "SINGLE",
                paymentMethods = listOf(
                    PaymentMethodRequest(
                        methodType = "BNPL",
                        amount = BigDecimal("1798000.00")
                    )
                )
            )
        )
    }
    
    private fun createTestMember(): MemberInfo {
        return MemberInfo(
            id = 1L,
            username = "test1",
            email = "test1@example.com",
            name = "테스트 사용자1",
            cashpointBalance = BigDecimal("50000"),
            status = "ACTIVE"
        )
    }
    
    private fun createTestProduct(): ProductInfo {
        return ProductInfo(
            id = 1L,
            name = "스마트폰",
            description = "최신 스마트폰입니다.",
            price = BigDecimal("899000.00"),
            stockQuantity = 50,
            sellerId = 3L,
            status = "ACTIVE"
        )
    }
}
