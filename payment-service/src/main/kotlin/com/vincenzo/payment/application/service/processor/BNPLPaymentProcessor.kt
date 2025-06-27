package com.vincenzo.payment.application.service.processor

import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import kotlinx.coroutines.delay
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * BNPL 결제 프로세서 (Mock 구현)
 */
@Component
class BNPLPaymentProcessor(
    @Value("\${payment.mock.bnpl.success-rate:85}")
    private val successRate: Int
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.BNPL
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock BNPL 처리 시뮬레이션
        delay(1500) // BNPL 승인 시간 시뮬레이션
        
        val isSuccess = Random.nextInt(100) < successRate
        
        return if (isSuccess) {
            val transactionId = "BNPL_${System.currentTimeMillis()}_${memberId}"
            PaymentProcessResult(
                success = true,
                message = "BNPL 결제가 승인되었습니다.",
                externalTransactionId = transactionId
            )
        } else {
            PaymentProcessResult(
                success = false,
                message = "BNPL 결제가 거부되었습니다. 신용 한도 초과"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock BNPL 취소 처리
        delay(800)
        
        return PaymentProcessResult(
            success = true,
            message = "BNPL 결제가 취소되었습니다.",
            externalTransactionId = paymentMethod.externalTransactionId
        )
    }
}
