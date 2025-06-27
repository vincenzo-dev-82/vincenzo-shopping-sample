package com.vincenzo.product.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 상품 도메인 모델
 */
data class Product(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stockQuantity: Int,
    val sellerId: Long,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    /**
     * 재고 차감 가능 여부 확인
     */
    fun canDeductStock(quantity: Int): Boolean {
        return status == ProductStatus.ACTIVE && 
               stockQuantity >= quantity && 
               quantity > 0
    }
    
    /**
     * 재고 차감
     */
    fun deductStock(quantity: Int): Product {
        require(canDeductStock(quantity)) { "재고가 부족합니다." }
        return copy(stockQuantity = stockQuantity - quantity)
    }
    
    /**
     * 재고 복원
     */
    fun restoreStock(quantity: Int): Product {
        require(quantity > 0) { "복원 수량은 0보다 커야 합니다." }
        return copy(stockQuantity = stockQuantity + quantity)
    }
}

/**
 * 상품 상태
 */
enum class ProductStatus {
    ACTIVE,     // 활성
    INACTIVE,   // 비활성
    SOLD_OUT    // 품절
}
