package com.vincenzo.product.adapter.`in`.web

import com.vincenzo.product.application.port.`in`.ProductUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "상품 관리 API")
class ProductController(
    private val productUseCase: ProductUseCase
) {
    
    @GetMapping("/{productId}")
    @Operation(summary = "상품 조회", description = "상품 정보를 조회합니다.")
    fun getProduct(@PathVariable productId: Long): ResponseEntity<ProductResponse> {
        val product = productUseCase.getProduct(productId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(ProductResponse.fromDomain(product))
    }
    
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다.")
    fun getProducts(@RequestParam productIds: List<Long>): ResponseEntity<List<ProductResponse>> {
        val products = productUseCase.getProducts(productIds)
        return ResponseEntity.ok(products.map { ProductResponse.fromDomain(it) })
    }
    
    @PostMapping("/{productId}/stock/check")
    @Operation(summary = "재고 확인", description = "상품 재고를 확인합니다.")
    fun checkStock(
        @PathVariable productId: Long,
        @RequestBody request: StockCheckRequest
    ): ResponseEntity<StockCheckResponse> {
        val available = productUseCase.checkStock(productId, request.quantity)
        return ResponseEntity.ok(StockCheckResponse(available))
    }
    
    @PostMapping("/{productId}/stock/deduct")
    @Operation(summary = "재고 차감", description = "상품 재고를 차감합니다.")
    fun deductStock(
        @PathVariable productId: Long,
        @RequestBody request: StockDeductRequest
    ): ResponseEntity<ProductResponse> {
        return try {
            val updatedProduct = productUseCase.deductStock(productId, request.quantity, request.transactionId)
            ResponseEntity.ok(ProductResponse.fromDomain(updatedProduct))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PostMapping("/{productId}/stock/restore")
    @Operation(summary = "재고 복원", description = "상품 재고를 복원합니다.")
    fun restoreStock(
        @PathVariable productId: Long,
        @RequestBody request: StockRestoreRequest
    ): ResponseEntity<ProductResponse> {
        return try {
            val updatedProduct = productUseCase.restoreStock(productId, request.quantity, request.transactionId)
            ResponseEntity.ok(ProductResponse.fromDomain(updatedProduct))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}
