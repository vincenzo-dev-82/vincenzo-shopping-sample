package com.vincenzo.product.application.port.out

import com.vincenzo.product.domain.model.Product

/**
 * 상품 리포지토리 인터페이스
 */
interface ProductRepository {
    /**
     * 상품 조회
     */
    fun findById(productId: Long): Product?
    
    /**
     * 상품 목록 조회
     */
    fun findByIdIn(productIds: List<Long>): List<Product>
    
    /**
     * 상품 저장
     */
    fun save(product: Product): Product
    
    /**
     * 재고 업데이트 (동시성 제어)
     */
    fun updateStockWithLock(productId: Long, newStock: Int): Boolean
}
