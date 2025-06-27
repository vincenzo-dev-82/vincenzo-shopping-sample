package com.vincenzo.product.application.service

import com.vincenzo.product.application.port.`in`.ProductUseCase
import com.vincenzo.product.application.port.out.ProductRepository
import com.vincenzo.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) : ProductUseCase {
    
    @Transactional(readOnly = true)
    override fun getProduct(productId: Long): Product? {
        return productRepository.findById(productId)
    }
    
    @Transactional(readOnly = true)
    override fun getProducts(productIds: List<Long>): List<Product> {
        return productRepository.findByIdIn(productIds)
    }
    
    @Transactional(readOnly = true)
    override fun checkStock(productId: Long, quantity: Int): Boolean {
        val product = productRepository.findById(productId)
            ?: return false
        return product.canDeductStock(quantity)
    }
    
    override fun deductStock(productId: Long, quantity: Int, transactionId: String): Product {
        val product = productRepository.findById(productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품입니다. ID: $productId")
        
        if (!product.canDeductStock(quantity)) {
            throw IllegalStateException("재고가 부족합니다. 요청: $quantity, 재고: ${product.stockQuantity}")
        }
        
        val updatedProduct = product.deductStock(quantity)
        
        // 동시성 제어를 위한 업데이트
        val success = productRepository.updateStockWithLock(productId, updatedProduct.stockQuantity)
        if (!success) {
            throw IllegalStateException("동시 업데이트로 인한 실패")
        }
        
        return updatedProduct
    }
    
    override fun restoreStock(productId: Long, quantity: Int, transactionId: String): Product {
        val product = productRepository.findById(productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품입니다. ID: $productId")
        
        val updatedProduct = product.restoreStock(quantity)
        
        // 동시성 제어를 위한 업데이트
        val success = productRepository.updateStockWithLock(productId, updatedProduct.stockQuantity)
        if (!success) {
            throw IllegalStateException("동시 업데이트로 인한 실패")
        }
        
        return updatedProduct
    }
}
