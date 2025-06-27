package com.vincenzo.payment.application.service.processor

import com.vincenzo.payment.application.port.out.MemberServiceClient
import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import org.springframework.stereotype.Component

/**
 * 캐시포인트 결제 프로세서
 */
@Component
class CashpointPaymentProcessor(
    private val memberServiceClient: MemberServiceClient
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.CASHPOINT
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        val transactionId = "CP_${System.currentTimeMillis()}_${memberId}"
        
        return try {
            val success = memberServiceClient.deductCashpoint(
                memberId = memberId,
                amount = paymentMethod.amount,
                transactionId = transactionId
            )
            
            if (success) {
                PaymentProcessResult(
                    success = true,
                    message = "캐시포인트 결제가 성공했습니다.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "캐시포인트 잔액이 부족합니다."
                )
            }
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "캐시포인트 결제 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        val transactionId = paymentMethod.externalTransactionId ?: "REFUND_${System.currentTimeMillis()}"
        
        return try {
            val success = memberServiceClient.refundCashpoint(
                memberId = memberId,
                amount = paymentMethod.amount,
                transactionId = transactionId
            )
            
            if (success) {
                PaymentProcessResult(
                    success = true,
                    message = "캐시포인트가 환불되었습니다.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "캐시포인트 환불에 실패했습니다."
                )
            }
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "캐시포인트 환불 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}
