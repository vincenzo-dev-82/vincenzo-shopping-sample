package com.vincenzo.member.application.port.`in`

import com.vincenzo.member.domain.model.Member
import java.math.BigDecimal

/**
 * 회원 관련 Use Case 인터페이스
 */
interface MemberUseCase {
    /**
     * 회원 조회
     */
    fun getMember(memberId: Long): Member?
    
    /**
     * 캐시포인트 잔액 조회
     */
    fun getCashpointBalance(memberId: Long): BigDecimal
    
    /**
     * 캐시포인트 차감
     */
    fun deductCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Member
    
    /**
     * 캐시포인트 환불
     */
    fun refundCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Member
}
