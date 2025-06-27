package com.vincenzo.payment.application.port.out

import com.vincenzo.payment.domain.event.PaymentEvent

/**
 * 이벤트 발행 인터페이스
 */
interface EventPublisher {
    /**
     * 결제 이벤트 발행
     */
    fun publishPaymentEvent(event: PaymentEvent)
}
