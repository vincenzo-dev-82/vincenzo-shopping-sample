package com.vincenzo.product.adapter.out.persistence

import com.vincenzo.product.domain.model.Product
import com.vincenzo.product.domain.model.ProductStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String?,
    
    @Column(nullable = false, precision = 12, scale = 2)
    val price: BigDecimal,
    
    @Column(name = "stock_quantity")
    var stockQuantity: Int,
    
    @Column(name = "seller_id", nullable = false)
    val sellerId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: ProductStatus = ProductStatus.ACTIVE,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Product {
        return Product(
            id = id,
            name = name,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            sellerId = sellerId,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                stockQuantity = product.stockQuantity,
                sellerId = product.sellerId,
                status = product.status,
                createdAt = product.createdAt ?: LocalDateTime.now(),
                updatedAt = product.updatedAt ?: LocalDateTime.now()
            )
        }
    }
}
