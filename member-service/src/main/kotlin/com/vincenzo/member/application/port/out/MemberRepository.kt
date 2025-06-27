package com.vincenzo.member.application.port.out

import com.vincenzo.member.domain.model.Member

/**
 * 회원 리포지토리 인터페이스
 */
interface MemberRepository {
    /**
     * 회원 조회
     */
    fun findById(memberId: Long): Member?
    
    /**
     * 회원 저장
     */
    fun save(member: Member): Member
    
    /**
     * 캐시포인트 업데이트 (동시성 제어)
     */
    fun updateCashpointWithLock(memberId: Long, newBalance: java.math.BigDecimal): Boolean
}
