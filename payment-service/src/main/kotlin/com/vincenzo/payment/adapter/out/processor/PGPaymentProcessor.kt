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
 * PG 결제 처리기 (Mock 구현)
 */
@Component
class PGPaymentProcessor(
    private val mockConfig: MockPGConfig
) : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.PG
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            // Mock PG 처리 시뮬레이션
            delay(1000) // 네트워크 지연 시뮬레이션
            
            val isSuccess = Random.nextInt(100) < mockConfig.successRate
            
            if (isSuccess) {
                val transactionId = "PG_${System.currentTimeMillis()}_${Random.nextInt(10000)}"
                PaymentProcessResult(
                    success = true,
                    message = "PG 결제가 성공적으로 완료되었습니다.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "PG 결제가 실패했습니다. 카드 정보를 확인해주세요."
                )
            }
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "PG 결제 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            delay(500) // 네트워크 지연 시뮬레이션
            
            PaymentProcessResult(
                success = true,
                message = "PG 결제가 성공적으로 취소되었습니다."
            )
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "PG 결제 취소 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}

@Component
@ConfigurationProperties(prefix = "payment.mock.pg")
class MockPGConfig {
    var enabled: Boolean = true
    var successRate: Int = 90 // 90% 성공률
}
