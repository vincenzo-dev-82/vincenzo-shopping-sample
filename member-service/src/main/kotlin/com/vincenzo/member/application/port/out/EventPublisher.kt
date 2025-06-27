package com.vincenzo.member.application.port.out

import com.vincenzo.member.domain.event.CashpointEvent

/**
 * 이벤트 발행 인터페이스
 */
interface EventPublisher {
    /**
     * 캐시포인트 이벤트 발행
     */
    fun publishCashpointEvent(event: CashpointEvent)
}
