package com.vincenzo.order.application.port.out

import com.vincenzo.order.domain.model.Order

/**
 * 주문 리포지토리 인터페이스
 */
interface OrderRepository {
    /**
     * 주문 저장
     */
    fun save(order: Order): Order
    
    /**
     * 주문 조회
     */
    fun findById(orderId: Long): Order?
    
    /**
     * 주문 번호로 조회
     */
    fun findByOrderNumber(orderNumber: String): Order?
    
    /**
     * 회원의 주문 목록 조회
     */
    fun findByMemberId(memberId: Long): List<Order>
}
