package com.vincenzo.order.adapter.out.external

import com.vincenzo.grpc.payment.*
import com.vincenzo.order.application.port.out.PaymentRequest
import com.vincenzo.order.application.port.out.PaymentResult
import com.vincenzo.order.application.port.out.PaymentServiceClient
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class PaymentServiceClientAdapter : PaymentServiceClient {
    
    @GrpcClient("payment-service")
    private lateinit var paymentServiceStub: PaymentServiceGrpcKt.PaymentServiceCoroutineStub
    
    override suspend fun processPayment(request: PaymentRequest): PaymentResult {
        return try {
            val grpcRequest = ProcessPaymentRequest.newBuilder()
                .setPaymentKey(request.paymentKey)
                .setOrderId(request.orderId)
                .setMemberId(request.memberId)
                .setTotalAmount(request.totalAmount.toDouble())
                .setPaymentType(
                    when (request.paymentType) {
                        "SINGLE" -> PaymentType.SINGLE
                        "COMBINED" -> PaymentType.COMBINED
                        else -> PaymentType.SINGLE
                    }
                )
                .addAllPaymentMethods(
                    request.paymentMethods.map { method ->
                        PaymentMethodInfo.newBuilder()
                            .setMethodType(
                                when (method.methodType) {
                                    "PG" -> PaymentMethodType.PG
                                    "CASHPOINT" -> PaymentMethodType.CASHPOINT
                                    "COUPON" -> PaymentMethodType.COUPON
                                    "BNPL" -> PaymentMethodType.BNPL
                                    else -> PaymentMethodType.PG
                                }
                            )
                            .setAmount(method.amount.toDouble())
                            .putAllAdditionalInfo(method.additionalInfo)
                            .build()
                    }
                )
                .build()
            
            val response = paymentServiceStub.processPayment(grpcRequest)
            
            PaymentResult(
                success = response.success,
                message = response.message,
                paymentKey = response.paymentKey.takeIf { it.isNotBlank() },
                status = response.status.name.takeIf { response.hasStatus() }
            )
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                message = e.message ?: "결제 처리 중 오류가 발생했습니다."
            )
        }
    }
}
