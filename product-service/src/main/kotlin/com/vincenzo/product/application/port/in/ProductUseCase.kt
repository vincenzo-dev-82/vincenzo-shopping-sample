package com.vincenzo.product.application.port.`in`

import com.vincenzo.product.domain.model.Product

/**
 * 상품 관련 Use Case 인터페이스
 */
interface ProductUseCase {
    /**
     * 상품 조회
     */
    fun getProduct(productId: Long): Product?
    
    /**
     * 상품 목록 조회
     */
    fun getProducts(productIds: List<Long>): List<Product>
    
    /**
     * 재고 확인
     */
    fun checkStock(productId: Long, quantity: Int): Boolean
    
    /**
     * 재고 차감
     */
    fun deductStock(productId: Long, quantity: Int, transactionId: String): Product
    
    /**
     * 재고 복원
     */
    fun restoreStock(productId: Long, quantity: Int, transactionId: String): Product
}
