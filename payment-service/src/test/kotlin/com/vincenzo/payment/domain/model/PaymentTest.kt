package com.vincenzo.payment.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PaymentTest {

    @Test
    fun `단일 PG 결제 생성 성공`() {
        // Given
        val paymentMethod = PaymentMethod(
            methodType = PaymentMethodType.PG,
            amount = BigDecimal.valueOf(10000)
        )
        
        // When
        val payment = Payment(
            paymentKey = "TEST_PAYMENT_KEY",
            orderId = 1L,
            totalAmount = BigDecimal.valueOf(10000),
            paymentType = PaymentType.SINGLE,
            paymentMethods = listOf(paymentMethod)
        )
        
        // Then
        assertThat(payment.paymentKey).isEqualTo("TEST_PAYMENT_KEY")
        assertThat(payment.orderId).isEqualTo(1L)
        assertThat(payment.totalAmount).isEqualTo(BigDecimal.valueOf(10000))
        assertThat(payment.paymentType).isEqualTo(PaymentType.SINGLE)
        assertThat(payment.status).isEqualTo(PaymentStatus.PENDING)
    }
    
    @Test
    fun `쿠폰 단독 결제 시 예외 발생`() {
        // Given
        val paymentMethod = PaymentMethod(
            methodType = PaymentMethodType.COUPON,
            amount = BigDecimal.valueOf(5000)
        )
        
        // When & Then
        assertThatThrownBy {
            Payment(
                paymentKey = "TEST_PAYMENT_KEY",
                orderId = 1L,
                totalAmount = BigDecimal.valueOf(5000),
                paymentType = PaymentType.SINGLE,
                paymentMethods = listOf(paymentMethod)
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("쿠폰은 단독 결제가 불가능합니다.")
    }
    
    @Test
    fun `복합 결제 성공`() {
        // Given
        val pgMethod = PaymentMethod(
            methodType = PaymentMethodType.PG,
            amount = BigDecimal.valueOf(8000)
        )
        val cashpointMethod = PaymentMethod(
            methodType = PaymentMethodType.CASHPOINT,
            amount = BigDecimal.valueOf(2000)
        )
        
        // When
        val payment = Payment(
            paymentKey = "TEST_PAYMENT_KEY",
            orderId = 1L,
            totalAmount = BigDecimal.valueOf(10000),
            paymentType = PaymentType.COMBINED,
            paymentMethods = listOf(pgMethod, cashpointMethod)
        )
        
        // Then
        assertThat(payment.paymentMethods).hasSize(2)
        assertThat(payment.paymentType).isEqualTo(PaymentType.COMBINED)
    }
    
    @Test
    fun `PG 없는 복합 결제 시 예외 발생`() {
        // Given
        val cashpointMethod = PaymentMethod(
            methodType = PaymentMethodType.CASHPOINT,
            amount = BigDecimal.valueOf(5000)
        )
        val couponMethod = PaymentMethod(
            methodType = PaymentMethodType.COUPON,
            amount = BigDecimal.valueOf(5000)
        )
        
        // When & Then
        assertThatThrownBy {
            Payment(
                paymentKey = "TEST_PAYMENT_KEY",
                orderId = 1L,
                totalAmount = BigDecimal.valueOf(10000),
                paymentType = PaymentType.COMBINED,
                paymentMethods = listOf(cashpointMethod, couponMethod)
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("복합 결제에는 PG 결제가 포함되어야 합니다.")
    }
}
