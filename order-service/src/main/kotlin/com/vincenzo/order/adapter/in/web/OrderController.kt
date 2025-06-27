package com.vincenzo.order.adapter.`in`.web

import com.vincenzo.order.application.port.`in`.OrderUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 관리 API")
class OrderController(
    private val orderUseCase: OrderUseCase
) {
    
    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    fun createOrder(@RequestBody request: CreateOrderWebRequest): ResponseEntity<OrderResponse> {
        return try {
            val order = orderUseCase.createOrder(request.toUseCaseRequest())
            ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.fromDomain(order))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(OrderResponse.error(e.message ?: "주문 생성 실패"))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest()
                .body(OrderResponse.error(e.message ?: "주문 생성 실패"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(OrderResponse.error("서버 오류가 발생했습니다."))
        }
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 ID로 주문을 조회합니다.")
    fun getOrder(@PathVariable orderId: Long): ResponseEntity<OrderResponse> {
        val order = orderUseCase.getOrder(orderId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(OrderResponse.fromDomain(order))
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "주문 번호로 조회", description = "주문 번호로 주문을 조회합니다.")
    fun getOrderByOrderNumber(@PathVariable orderNumber: String): ResponseEntity<OrderResponse> {
        val order = orderUseCase.getOrderByOrderNumber(orderNumber)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(OrderResponse.fromDomain(order))
    }
    
    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원 주문 목록", description = "회원의 주문 목록을 조회합니다.")
    fun getOrdersByMember(@PathVariable memberId: Long): ResponseEntity<List<OrderResponse>> {
        val orders = orderUseCase.getOrdersByMember(memberId)
        return ResponseEntity.ok(orders.map { OrderResponse.fromDomain(it) })
    }
    
    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "주문 확정", description = "주문을 확정합니다.")
    fun confirmOrder(@PathVariable orderId: Long): ResponseEntity<OrderResponse> {
        return try {
            val order = orderUseCase.confirmOrder(orderId)
            ResponseEntity.ok(OrderResponse.fromDomain(order))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(OrderResponse.error(e.message ?: "주문 확정 실패"))
        }
    }
    
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    fun cancelOrder(
        @PathVariable orderId: Long,
        @RequestBody request: CancelOrderRequest
    ): ResponseEntity<OrderResponse> {
        return try {
            val order = orderUseCase.cancelOrder(orderId, request.reason)
            ResponseEntity.ok(OrderResponse.fromDomain(order))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(OrderResponse.error(e.message ?: "주문 취소 실패"))
        }
    }
    
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "주문 완료", description = "주문을 완료합니다.")
    fun completeOrder(@PathVariable orderId: Long): ResponseEntity<OrderResponse> {
        return try {
            val order = orderUseCase.completeOrder(orderId)
            ResponseEntity.ok(OrderResponse.fromDomain(order))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(OrderResponse.error(e.message ?: "주문 완료 실패"))
        }
    }
}
