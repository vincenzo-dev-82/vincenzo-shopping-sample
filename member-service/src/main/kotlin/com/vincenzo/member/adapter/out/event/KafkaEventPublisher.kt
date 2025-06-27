package com.vincenzo.member.adapter.out.event

import com.vincenzo.member.application.port.out.EventPublisher
import com.vincenzo.member.domain.event.CashpointEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {
    
    companion object {
        private const val CASHPOINT_TOPIC = "cashpoint-events"
    }
    
    override fun publishCashpointEvent(event: CashpointEvent) {
        kafkaTemplate.send(CASHPOINT_TOPIC, event.memberId.toString(), event)
    }
}
