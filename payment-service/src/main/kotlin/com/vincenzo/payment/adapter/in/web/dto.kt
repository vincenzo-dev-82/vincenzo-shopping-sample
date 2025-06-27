package com.vincenzo.payment.adapter.`in`.web

import com.vincenzo.payment.application.port.`in`.*
import com.vincenzo.payment.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 처리 웹 요청 DTO
 */
data class ProcessPaymentWebRequest(
    val paymentKey: String,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: BigDecimal,
    val paymentType: String,
    val paymentMethods: List<PaymentMethodWebRequest>
) {
    fun toUseCaseRequest(): com.vincenzo.payment.application.port.`in`.ProcessPaymentRequest {
        return com.vincenzo.payment.application.port.`in`.ProcessPaymentRequest(
            paymentKey = paymentKey,
            orderId = orderId,
            memberId = memberId,
            totalAmount = totalAmount,
            paymentType = when (paymentType) {
                "SINGLE" -> PaymentType.SINGLE
                "COMBINED" -> PaymentType.COMBINED
                else -> PaymentType.SINGLE
            },
            paymentMethods = paymentMethods.map { it.toUseCaseRequest() }
        )
    }
}

/**
 * 결제 방법 웹 요청 DTO
 */
data class PaymentMethodWebRequest(
    val methodType: String,
    val amount: BigDecimal,
    val additionalInfo: Map<String, String> = emptyMap()
) {
    fun toUseCaseRequest(): com.vincenzo.payment.application.port.`in`.PaymentMethodRequest {
        return com.vincenzo.payment.application.port.`in`.PaymentMethodRequest(
            methodType = when (methodType) {
                "PG" -> PaymentMethodType.PG
                "CASHPOINT" -> PaymentMethodType.CASHPOINT
                "COUPON" -> PaymentMethodType.COUPON
                "BNPL" -> PaymentMethodType.BNPL
                else -> PaymentMethodType.PG
            },
            amount = amount,
            additionalInfo = additionalInfo
        )
    }
}

/**
 * 결제 취소 웹 요청 DTO
 */
data class CancelPaymentWebRequest(
    val reason: String?
)

/**
 * 결제 응답 DTO
 */
data class PaymentResponse(
    val success: Boolean,
    val message: String,
    val payment: PaymentDto? = null
) {
    companion object {
        fun success(message: String, payment: PaymentDto? = null): PaymentResponse {
            return PaymentResponse(true, message, payment)
        }
        
        fun failure(message: String): PaymentResponse {
            return PaymentResponse(false, message)
        }
    }
}

/**
 * 결제 DTO
 */
data class PaymentDto(
    val id: Long?,
    val paymentKey: String,
    val orderId: Long,
    val totalAmount: BigDecimal,
    val paymentType: PaymentType,
    val status: PaymentStatus,
    val paymentMethods: List<PaymentMethodDto>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(payment: Payment): PaymentDto {
            return PaymentDto(
                id = payment.id,
                paymentKey = payment.paymentKey,
                orderId = payment.orderId,
                totalAmount = payment.totalAmount,
                paymentType = payment.paymentType,
                status = payment.status,
                paymentMethods = payment.paymentMethods.map { PaymentMethodDto.fromDomain(it) },
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt
            )
        }
    }
}

/**
 * 결제 방법 DTO
 */
data class PaymentMethodDto(
    val id: Long?,
    val methodType: PaymentMethodType,
    val amount: BigDecimal,
    val status: PaymentMethodStatus,
    val externalTransactionId: String?
) {
    companion object {
        fun fromDomain(paymentMethod: PaymentMethod): PaymentMethodDto {
            return PaymentMethodDto(
                id = paymentMethod.id,
                methodType = paymentMethod.methodType,
                amount = paymentMethod.amount,
                status = paymentMethod.status,
                externalTransactionId = paymentMethod.externalTransactionId
            )
        }
    }
}
