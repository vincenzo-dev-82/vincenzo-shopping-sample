package com.vincenzo.order.application.port.out

import java.math.BigDecimal

/**
 * 상품 서비스 클라이언트 인터페이스
 */
interface ProductServiceClient {
    /**
     * 상품 조회
     */
    suspend fun getProduct(productId: Long): ProductInfo?
    
    /**
     * 상품 목록 조회
     */
    suspend fun getProducts(productIds: List<Long>): List<ProductInfo>
    
    /**
     * 재고 확인
     */
    suspend fun checkStock(productId: Long, quantity: Int): Boolean
}

/**
 * 상품 정보
 */
data class ProductInfo(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stockQuantity: Int,
    val sellerId: Long,
    val status: String
)
