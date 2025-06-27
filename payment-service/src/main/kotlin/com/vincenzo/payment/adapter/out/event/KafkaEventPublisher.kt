package com.vincenzo.payment.adapter.out.event

import com.vincenzo.payment.application.port.out.EventPublisher
import com.vincenzo.payment.domain.event.PaymentEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {
    
    companion object {
        private const val PAYMENT_TOPIC = "payment-events"
    }
    
    override fun publishPaymentEvent(event: PaymentEvent) {
        kafkaTemplate.send(PAYMENT_TOPIC, event.paymentKey, event)
    }
}
