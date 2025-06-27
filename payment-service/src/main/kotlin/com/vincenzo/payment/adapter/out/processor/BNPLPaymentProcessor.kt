package com.vincenzo.payment.adapter.out.processor

import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import kotlinx.coroutines.delay
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * BNPL (Buy Now Pay Later) 결제 처리기 (Mock 구현)
 */
@Component
class BNPLPaymentProcessor(
    private val mockConfig: MockBNPLConfig
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.BNPL
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            // Mock BNPL 신용평가 및 승인 시뮬레이션
            delay(2000) // 신용평가 시간 시뮬레이션
            
            val isApproved = Random.nextInt(100) < mockConfig.successRate
            
            if (isApproved) {
                val transactionId = "BNPL_${memberId}_${System.currentTimeMillis()}"
                PaymentProcessResult(
                    success = true,
                    message = "BNPL 결제가 승인되었습니다. 나중에 결제하세요.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "BNPL 신용평가가 거부되었습니다."
                )
            }
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "BNPL 결제 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            delay(1000) // BNPL 취소 시뮬레이션
            
            PaymentProcessResult(
                success = true,
                message = "BNPL 결제가 성공적으로 취소되었습니다."
            )
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "BNPL 취소 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}

@Component
@ConfigurationProperties(prefix = "payment.mock.bnpl")
class MockBNPLConfig {
    var enabled: Boolean = true
    var successRate: Int = 85 // 85% 성공률
}
