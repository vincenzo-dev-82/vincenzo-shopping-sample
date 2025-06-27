package com.vincenzo.payment.application.service.processor

import com.vincenzo.payment.application.port.out.PaymentProcessResult
import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethod
import com.vincenzo.payment.domain.model.PaymentMethodType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

/**
 * 쿠폰 결제 프로세서 (Mock 구현)
 */
@Component
class CouponPaymentProcessor : PaymentProcessor {
    
    override fun supports(methodType: PaymentMethodType): Boolean {
        return methodType == PaymentMethodType.COUPON
    }
    
    override suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock 쿠폰 처리 시뮬레이션
        delay(300)
        
        val couponCode = paymentMethod.additionalInfo["couponCode"]
        
        return if (!couponCode.isNullOrBlank()) {
            val transactionId = "COUPON_${couponCode}_${System.currentTimeMillis()}"
            PaymentProcessResult(
                success = true,
                message = "쿠폰이 적용되었습니다.",
                externalTransactionId = transactionId
            )
        } else {
            PaymentProcessResult(
                success = false,
                message = "유효하지 않은 쿠폰입니다."
            )
        }
    }
    
    override suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult {
        // Mock 쿠폰 취소 처리 (쿠폰 복원)
        delay(100)
        
        return PaymentProcessResult(
            success = true,
            message = "쿠폰이 복원되었습니다.",
            externalTransactionId = paymentMethod.externalTransactionId
        )
    }
}
