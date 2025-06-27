package com.vincenzo.payment.application.port.out

import java.math.BigDecimal

/**
 * 회원 서비스 클라이언트 인터페이스
 */
interface MemberServiceClient {
    /**
     * 캐시포인트 차감
     */
    suspend fun deductCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Boolean
    
    /**
     * 캐시포인트 환불
     */
    suspend fun refundCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Boolean
}
