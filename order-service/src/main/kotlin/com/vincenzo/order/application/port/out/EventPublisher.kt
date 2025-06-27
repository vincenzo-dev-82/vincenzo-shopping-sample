package com.vincenzo.order.application.port.out

import com.vincenzo.order.domain.event.OrderEvent

/**
 * 이벤트 발행 인터페이스
 */
interface EventPublisher {
    /**
     * 주문 이벤트 발행
     */
    fun publishOrderEvent(event: OrderEvent)
}
