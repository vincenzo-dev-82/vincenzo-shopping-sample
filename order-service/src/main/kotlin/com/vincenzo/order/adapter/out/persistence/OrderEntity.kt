package com.vincenzo.order.adapter.out.persistence

import com.vincenzo.order.domain.model.Order
import com.vincenzo.order.domain.model.OrderItem
import com.vincenzo.order.domain.model.OrderStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    val orderNumber: String,
    
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal,
    
    @Column(name = "discount_amount", precision = 12, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    val finalAmount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: OrderStatus = OrderStatus.PENDING,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val orderItems: MutableList<OrderItemEntity> = mutableListOf(),
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Order {
        return Order(
            id = id,
            orderNumber = orderNumber,
            memberId = memberId,
            orderItems = orderItems.map { it.toDomain() },
            totalAmount = totalAmount,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(order: Order): OrderEntity {
            val entity = OrderEntity(
                id = order.id,
                orderNumber = order.orderNumber,
                memberId = order.memberId,
                totalAmount = order.totalAmount,
                discountAmount = order.discountAmount,
                finalAmount = order.finalAmount,
                status = order.status,
                createdAt = order.createdAt ?: LocalDateTime.now(),
                updatedAt = order.updatedAt ?: LocalDateTime.now()
            )
            
            order.orderItems.forEach { orderItem ->
                val itemEntity = OrderItemEntity.fromDomain(orderItem, entity)
                entity.orderItems.add(itemEntity)
            }
            
            return entity
        }
    }
}

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: OrderEntity,
    
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,
    
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    val totalPrice: BigDecimal
) {
    fun toDomain(): OrderItem {
        return OrderItem(
            id = id,
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice
        )
    }
    
    companion object {
        fun fromDomain(orderItem: OrderItem, orderEntity: OrderEntity): OrderItemEntity {
            return OrderItemEntity(
                id = orderItem.id,
                order = orderEntity,
                productId = orderItem.productId,
                quantity = orderItem.quantity,
                unitPrice = orderItem.unitPrice,
                totalPrice = orderItem.totalPrice
            )
        }
    }
}
