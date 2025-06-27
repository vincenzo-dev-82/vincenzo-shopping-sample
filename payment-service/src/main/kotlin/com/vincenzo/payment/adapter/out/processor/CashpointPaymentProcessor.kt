package com.vincenzo.payment.adapter.out.processor

import com.vincenzo.payment.application.port.out.MemberServiceClient
import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import org.springframework.stereotype.Component

/**
 * 캐시포인트 결제 처리기
 */
@Component
class CashpointPaymentProcessor(
    private val memberServiceClient: MemberServiceClient
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.CASHPOINT
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            val transactionId = "CASHPOINT_${System.currentTimeMillis()}_${memberId}"
            
            val success = memberServiceClient.deductCashpoint(
                memberId = memberId,
                amount = paymentMethod.amount,
                transactionId = transactionId
            )
            
            if (success) {
                PaymentProcessResult(
                    success = true,
                    message = "캐시포인트 결제가 성공적으로 완료되었습니다.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "캐시포인트 잔액이 부족하거나 결제가 실패했습니다."
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
        return try {
            val transactionId = paymentMethod.externalTransactionId
                ?: return PaymentProcessResult(
                    success = false,
                    message = "취소할 외부 거래 ID가 없습니다."
                )
            
            val success = memberServiceClient.refundCashpoint(
                memberId = memberId,
                amount = paymentMethod.amount,
                transactionId = transactionId
            )
            
            if (success) {
                PaymentProcessResult(
                    success = true,
                    message = "캐시포인트가 성공적으로 환불되었습니다."
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "캐시포인트 환불이 실패했습니다."
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
