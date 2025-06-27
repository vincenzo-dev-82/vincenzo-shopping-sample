package com.vincenzo.payment.adapter.out.processor

import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 쿠폰 결제 처리기 (Mock 구현)
 */
@Component
class CouponPaymentProcessor : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.COUPON
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            delay(200) // 쿠폰 검증 시뮬레이션
            
            // 쿠폰 코드 검증 (Mock)
            val couponCode = paymentMethod.additionalInfo["couponCode"]
            if (couponCode.isNullOrBlank()) {
                return PaymentProcessResult(
                    success = false,
                    message = "쿠폰 코드가 없습니다."
                )
            }
            
            // Mock 쿠폰 유효성 검증 (90% 성공률)
            val isValid = Random.nextInt(100) < 90
            
            if (isValid) {
                val transactionId = "COUPON_${couponCode}_${System.currentTimeMillis()}"
                PaymentProcessResult(
                    success = true,
                    message = "쿠폰 할인이 성공적으로 적용되었습니다.",
                    externalTransactionId = transactionId
                )
            } else {
                PaymentProcessResult(
                    success = false,
                    message = "유효하지 않은 쿠폰입니다."
                )
            }
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "쿠폰 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        return try {
            delay(100)
            
            PaymentProcessResult(
                success = true,
                message = "쿠폰 할인이 성공적으로 취소되었습니다."
            )
        } catch (e: Exception) {
            PaymentProcessResult(
                success = false,
                message = "쿠폰 취소 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}
