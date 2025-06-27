package com.vincenzo.order.adapter.out.event

import com.vincenzo.order.application.port.out.EventPublisher
import com.vincenzo.order.domain.event.OrderEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {
    
    companion object {
        private const val ORDER_TOPIC = "order-events"
    }
    
    override fun publishOrderEvent(event: OrderEvent) {
        kafkaTemplate.send(ORDER_TOPIC, event.orderNumber, event)
    }
}
