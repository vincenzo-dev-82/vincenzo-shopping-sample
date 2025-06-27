package com.vincenzo.payment.adapter.`in`.grpc

import com.vincenzo.grpc.payment.*
import com.vincenzo.payment.application.port.`in`.*
import com.vincenzo.payment.domain.model.PaymentMethodType
import com.vincenzo.payment.domain.model.PaymentType
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import java.math.BigDecimal

@GrpcService
class PaymentGrpcService(
    private val paymentUseCase: PaymentUseCase
) : PaymentServiceGrpcKt.PaymentServiceCoroutineImplBase() {
    
    override suspend fun processPayment(request: ProcessPaymentRequest): ProcessPaymentResponse {
        return try {
            val useCaseRequest = ProcessPaymentRequest(
                paymentKey = request.paymentKey,
                orderId = request.orderId,
                memberId = request.memberId,
                totalAmount = BigDecimal.valueOf(request.totalAmount),
                paymentType = when (request.paymentType) {
                    PaymentType.SINGLE -> com.vincenzo.payment.domain.model.PaymentType.SINGLE
                    PaymentType.COMBINED -> com.vincenzo.payment.domain.model.PaymentType.COMBINED
                    else -> com.vincenzo.payment.domain.model.PaymentType.SINGLE
                },
                paymentMethods = request.paymentMethodsList.map { method ->
                    PaymentMethodRequest(
                        methodType = when (method.methodType) {
                            PaymentMethodType.PG -> com.vincenzo.payment.domain.model.PaymentMethodType.PG
                            PaymentMethodType.CASHPOINT -> com.vincenzo.payment.domain.model.PaymentMethodType.CASHPOINT
                            PaymentMethodType.COUPON -> com.vincenzo.payment.domain.model.PaymentMethodType.COUPON
                            PaymentMethodType.BNPL -> com.vincenzo.payment.domain.model.PaymentMethodType.BNPL
                            else -> com.vincenzo.payment.domain.model.PaymentMethodType.PG
                        },
                        amount = BigDecimal.valueOf(method.amount),
                        additionalInfo = method.additionalInfoMap
                    )
                }
            )
            
            val result = paymentUseCase.processPayment(useCaseRequest)
            
            ProcessPaymentResponse.newBuilder()
                .setSuccess(result.success)
                .setMessage(result.message)
                .setPaymentKey(request.paymentKey)
                .setStatus(
                    if (result.success) com.vincenzo.grpc.payment.PaymentStatus.COMPLETED
                    else com.vincenzo.grpc.payment.PaymentStatus.FAILED
                )
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun cancelPayment(request: CancelPaymentRequest): CancelPaymentResponse {
        return try {
            val payment = paymentUseCase.cancelPayment(request.paymentKey, request.reason)
            
            CancelPaymentResponse.newBuilder()
                .setSuccess(true)
                .setMessage("결제가 성공적으로 취소되었습니다.")
                .build()
        } catch (e: Exception) {
            CancelPaymentResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.message ?: "결제 취소에 실패했습니다.")
                .build()
        }
    }
    
    override suspend fun getPaymentStatus(request: GetPaymentStatusRequest): GetPaymentStatusResponse {
        return try {
            val payment = paymentUseCase.getPaymentStatus(request.paymentKey)
            
            if (payment != null) {
                GetPaymentStatusResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("성공")
                    .setStatus(
                        when (payment.status) {
                            com.vincenzo.payment.domain.model.PaymentStatus.PENDING -> com.vincenzo.grpc.payment.PaymentStatus.PENDING
                            com.vincenzo.payment.domain.model.PaymentStatus.COMPLETED -> com.vincenzo.grpc.payment.PaymentStatus.COMPLETED
                            com.vincenzo.payment.domain.model.PaymentStatus.FAILED -> com.vincenzo.grpc.payment.PaymentStatus.FAILED
                            com.vincenzo.payment.domain.model.PaymentStatus.CANCELLED -> com.vincenzo.grpc.payment.PaymentStatus.CANCELLED
                        }
                    )
                    .setTotalAmount(payment.totalAmount.toDouble())
                    .build()
            } else {
                GetPaymentStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("결제 정보를 찾을 수 없습니다.")
                    .setStatus(com.vincenzo.grpc.payment.PaymentStatus.PENDING)
                    .setTotalAmount(0.0)
                    .build()
            }
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
}
