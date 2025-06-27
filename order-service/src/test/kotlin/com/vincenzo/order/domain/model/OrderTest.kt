package com.vincenzo.order.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderTest {

    @Test
    fun `주문 생성 성공`() {
        // Given
        val orderItem = OrderItem(
            productId = 1L,
            quantity = 2,
            unitPrice = BigDecimal.valueOf(10000)
        )
        
        // When
        val order = Order(
            memberId = 1L,
            orderItems = listOf(orderItem),
            totalAmount = BigDecimal.valueOf(20000)
        )
        
        // Then
        assertThat(order.memberId).isEqualTo(1L)
        assertThat(order.orderItems).hasSize(1)
        assertThat(order.totalAmount).isEqualTo(BigDecimal.valueOf(20000))
        assertThat(order.status).isEqualTo(OrderStatus.PENDING)
    }
    
    @Test
    fun `빈 주문 상품 리스트로 주문 생성 시 예외 발생`() {
        // When & Then
        assertThatThrownBy {
            Order(
                memberId = 1L,
                orderItems = emptyList(),
                totalAmount = BigDecimal.ZERO
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("주문 상품이 비어있습니다.")
    }
    
    @Test
    fun `주문 확정 성공`() {
        // Given
        val orderItem = OrderItem(
            productId = 1L,
            quantity = 1,
            unitPrice = BigDecimal.valueOf(10000)
        )
        val order = Order(
            memberId = 1L,
            orderItems = listOf(orderItem),
            totalAmount = BigDecimal.valueOf(10000),
            status = OrderStatus.PENDING
        )
        
        // When
        val confirmedOrder = order.confirm()
        
        // Then
        assertThat(confirmedOrder.status).isEqualTo(OrderStatus.CONFIRMED)
    }
    
    @Test
    fun `이미 확정된 주문 재확정 시 예외 발생`() {
        // Given
        val orderItem = OrderItem(
            productId = 1L,
            quantity = 1,
            unitPrice = BigDecimal.valueOf(10000)
        )
        val order = Order(
            memberId = 1L,
            orderItems = listOf(orderItem),
            totalAmount = BigDecimal.valueOf(10000),
            status = OrderStatus.CONFIRMED
        )
        
        // When & Then
        assertThatThrownBy {
            order.confirm()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("대기 중인 주문만 확정할 수 있습니다.")
    }
}
