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
 * PG 결제 프로세서 (Mock 구현)
 */
@Component
class PGPaymentProcessor(
    @Value("\${payment.mock.pg.success-rate:90}")
    private val successRate: Int
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.PG
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock PG 처리 시뮬레이션
        delay(1000) // PG 처리 시간 시뮬레이션
        
        val isSuccess = Random.nextInt(100) < successRate
        
        return if (isSuccess) {
            val transactionId = "PG_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
            PaymentProcessResult(
                success = true,
                message = "PG 결제가 성공했습니다.",
                externalTransactionId = transactionId
            )
        } else {
            PaymentProcessResult(
                success = false,
                message = "PG 결제가 실패했습니다. 카드사 승인 거부"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock PG 취소 처리
        delay(500)
        
        return PaymentProcessResult(
            success = true,
            message = "PG 결제가 취소되었습니다.",
            externalTransactionId = paymentMethod.externalTransactionId
        )
    }
}
