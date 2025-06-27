package com.vincenzo.payment.application.port.out

import com.vincenzo.payment.domain.model.Payment

/**
 * 결제 리포지토리 인터페이스
 */
interface PaymentRepository {
    /**
     * 결제 저장
     */
    fun save(payment: Payment): Payment
    
    /**
     * 결제 키로 조회
     */
    fun findByPaymentKey(paymentKey: String): Payment?
    
    /**
     * 주문 ID로 조회
     */
    fun findByOrderId(orderId: Long): Payment?
}
