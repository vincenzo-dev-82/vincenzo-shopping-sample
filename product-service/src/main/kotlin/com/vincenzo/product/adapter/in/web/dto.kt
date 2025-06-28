package com.vincenzo.product.adapter.`in`.web

import com.vincenzo.product.domain.model.Product
import com.vincenzo.product.domain.model.ProductStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 상품 응답 DTO
 */
data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stockQuantity: Int,
    val sellerId: Long,
    val status: ProductStatus,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id!!,
                name = product.name,
                description = product.description,
                price = product.price,
                stockQuantity = product.stockQuantity,
                sellerId = product.sellerId,
                status = product.status,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

/**
 * 재고 확인 요청 DTO
 */
data class StockCheckRequest(
    val quantity: Int
)

/**
 * 재고 확인 응답 DTO
 */
data class StockCheckResponse(
    val available: Boolean
)

/**
 * 재고 차감 요청 DTO
 */
data class StockDeductRequest(
    val quantity: Int,
    val transactionId: String
)

/**
 * 재고 복원 요청 DTO
 */
data class StockRestoreRequest(
    val quantity: Int,
    val transactionId: String
)
