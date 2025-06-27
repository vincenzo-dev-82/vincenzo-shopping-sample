package com.vincenzo.order.adapter.out.external

import com.vincenzo.grpc.product.*
import com.vincenzo.order.application.port.out.ProductInfo
import com.vincenzo.order.application.port.out.ProductServiceClient
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ProductServiceClientAdapter : ProductServiceClient {
    
    @GrpcClient("product-service")
    private lateinit var productServiceStub: ProductServiceGrpcKt.ProductServiceCoroutineStub
    
    override suspend fun getProduct(productId: Long): ProductInfo? {
        return try {
            val request = GetProductRequest.newBuilder()
                .setProductId(productId)
                .build()
            
            val response = productServiceStub.getProduct(request)
            
            if (response.success && response.hasProduct()) {
                val product = response.product
                ProductInfo(
                    id = product.id,
                    name = product.name,
                    description = product.description.takeIf { it.isNotBlank() },
                    price = BigDecimal.valueOf(product.price),
                    stockQuantity = product.stockQuantity,
                    sellerId = product.sellerId,
                    status = product.status
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getProducts(productIds: List<Long>): List<ProductInfo> {
        return try {
            val request = GetProductsRequest.newBuilder()
                .addAllProductIds(productIds)
                .build()
            
            val response = productServiceStub.getProducts(request)
            
            if (response.success) {
                response.productsList.map { product ->
                    ProductInfo(
                        id = product.id,
                        name = product.name,
                        description = product.description.takeIf { it.isNotBlank() },
                        price = BigDecimal.valueOf(product.price),
                        stockQuantity = product.stockQuantity,
                        sellerId = product.sellerId,
                        status = product.status
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun checkStock(productId: Long, quantity: Int): Boolean {
        return try {
            val request = CheckStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build()
            
            val response = productServiceStub.checkStock(request)
            
            response.success && response.available
        } catch (e: Exception) {
            false
        }
    }
}
