package com.vincenzo.product.adapter.`in`.grpc

import com.vincenzo.grpc.product.*
import com.vincenzo.product.application.port.`in`.ProductUseCase
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ProductGrpcService(
    private val productUseCase: ProductUseCase
) : ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    
    override suspend fun getProduct(request: GetProductRequest): GetProductResponse {
        return try {
            val product = productUseCase.getProduct(request.productId)
            if (product != null) {
                GetProductResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("성공")
                    .setProduct(
                        Product.newBuilder()
                            .setId(product.id!!)
                            .setName(product.name)
                            .setDescription(product.description ?: "")
                            .setPrice(product.price.toDouble())
                            .setStockQuantity(product.stockQuantity)
                            .setSellerId(product.sellerId)
                            .setStatus(product.status.name)
                            .build()
                    )
                    .build()
            } else {
                GetProductResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("상품을 찾을 수 없습니다.")
                    .build()
            }
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun getProducts(request: GetProductsRequest): GetProductsResponse {
        return try {
            val products = productUseCase.getProducts(request.productIdsList)
            GetProductsResponse.newBuilder()
                .setSuccess(true)
                .setMessage("성공")
                .addAllProducts(
                    products.map { product ->
                        Product.newBuilder()
                            .setId(product.id!!)
                            .setName(product.name)
                            .setDescription(product.description ?: "")
                            .setPrice(product.price.toDouble())
                            .setStockQuantity(product.stockQuantity)
                            .setSellerId(product.sellerId)
                            .setStatus(product.status.name)
                            .build()
                    }
                )
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun checkStock(request: CheckStockRequest): CheckStockResponse {
        return try {
            val available = productUseCase.checkStock(request.productId, request.quantity)
            val product = productUseCase.getProduct(request.productId)
            CheckStockResponse.newBuilder()
                .setSuccess(true)
                .setMessage("성공")
                .setAvailable(available)
                .setCurrentStock(product?.stockQuantity ?: 0)
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun deductStock(request: DeductStockRequest): DeductStockResponse {
        return try {
            val updatedProduct = productUseCase.deductStock(
                request.productId,
                request.quantity,
                request.transactionId
            )
            DeductStockResponse.newBuilder()
                .setSuccess(true)
                .setMessage("재고 차감 성공")
                .setRemainingStock(updatedProduct.stockQuantity)
                .build()
        } catch (e: IllegalStateException) {
            DeductStockResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.message ?: "재고 차감 실패")
                .setRemainingStock(0)
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun restoreStock(request: RestoreStockRequest): RestoreStockResponse {
        return try {
            val updatedProduct = productUseCase.restoreStock(
                request.productId,
                request.quantity,
                request.transactionId
            )
            RestoreStockResponse.newBuilder()
                .setSuccess(true)
                .setMessage("재고 복원 성공")
                .setCurrentStock(updatedProduct.stockQuantity)
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
}
