package com.vincenzo.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    
    fun findByOrderNumber(orderNumber: String): OrderEntity?
    
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderItems WHERE o.memberId = :memberId ORDER BY o.createdAt DESC")
    fun findByMemberIdWithItems(@Param("memberId") memberId: Long): List<OrderEntity>
    
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    fun findByIdWithItems(@Param("id") id: Long): OrderEntity?
}
