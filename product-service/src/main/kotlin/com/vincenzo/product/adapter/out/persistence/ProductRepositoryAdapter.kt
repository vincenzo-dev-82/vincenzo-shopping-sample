package com.vincenzo.product.adapter.out.persistence

import com.vincenzo.product.application.port.out.ProductRepository
import com.vincenzo.product.domain.model.Product
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryAdapter(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {
    
    override fun findById(productId: Long): Product? {
        return productJpaRepository.findById(productId)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByIdIn(productIds: List<Long>): List<Product> {
        return productJpaRepository.findByIdIn(productIds)
            .map { it.toDomain() }
    }
    
    override fun save(product: Product): Product {
        val entity = if (product.id != null) {
            productJpaRepository.findById(product.id)
                .map { existing ->
                    existing.stockQuantity = product.stockQuantity
                    existing
                }
                .orElse(ProductEntity.fromDomain(product))
        } else {
            ProductEntity.fromDomain(product)
        }
        
        return productJpaRepository.save(entity).toDomain()
    }
    
    override fun updateStockWithLock(productId: Long, newStock: Int): Boolean {
        return try {
            val updatedRows = productJpaRepository.updateStockQuantity(productId, newStock)
            updatedRows > 0
        } catch (e: Exception) {
            false
        }
    }
}
