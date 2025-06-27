package com.vincenzo.payment.application.service

import com.vincenzo.payment.application.port.out.PaymentProcessor
import com.vincenzo.payment.domain.model.PaymentMethodType
import org.springframework.stereotype.Component

/**
 * 결제 처리기 팩토리 (Strategy Pattern)
 */
@Component
class PaymentProcessorFactory(
    private val processors: List<PaymentProcessor>
) {
    
    /**
     * 결제 방법 타입에 따른 처리기 반환
     */
    fun getProcessor(methodType: PaymentMethodType): PaymentProcessor? {
        return processors.find { it.supports(methodType) }
    }
    
    /**
     * 지원하는 모든 결제 방법 타입 반환
     */
    fun getSupportedMethods(): Set<PaymentMethodType> {
        return processors.flatMap { processor ->
            PaymentMethodType.values().filter { processor.supports(it) }
        }.toSet()
    }
}
