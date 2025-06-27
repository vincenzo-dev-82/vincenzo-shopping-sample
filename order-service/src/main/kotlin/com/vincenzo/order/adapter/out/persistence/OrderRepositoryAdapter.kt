package com.vincenzo.order.adapter.out.persistence

import com.vincenzo.order.application.port.out.OrderRepository
import com.vincenzo.order.domain.model.Order
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {
    
    override fun save(order: Order): Order {
        val entity = if (order.id != null) {
            // 업데이트
            val existing = orderJpaRepository.findByIdWithItems(order.id)
            if (existing != null) {
                // 기존 엔티티의 상태만 업데이트
                val updated = OrderEntity(
                    id = existing.id,
                    orderNumber = existing.orderNumber,
                    memberId = existing.memberId,
                    totalAmount = existing.totalAmount,
                    discountAmount = existing.discountAmount,
                    finalAmount = existing.finalAmount,
                    status = order.status,
                    createdAt = existing.createdAt,
                    updatedAt = java.time.LocalDateTime.now()
                )
                // 기존 아이템들 복사
                existing.orderItems.forEach { item ->
                    updated.orderItems.add(item)
                }
                updated
            } else {
                OrderEntity.fromDomain(order)
            }
        } else {
            OrderEntity.fromDomain(order)
        }
        
        return orderJpaRepository.save(entity).toDomain()
    }
    
    override fun findById(orderId: Long): Order? {
        return orderJpaRepository.findByIdWithItems(orderId)?.toDomain()
    }
    
    override fun findByOrderNumber(orderNumber: String): Order? {
        return orderJpaRepository.findByOrderNumber(orderNumber)?.toDomain()
    }
    
    override fun findByMemberId(memberId: Long): List<Order> {
        return orderJpaRepository.findByMemberIdWithItems(memberId)
            .map { it.toDomain() }
    }
}
