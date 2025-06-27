package com.vincenzo.member.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 회원 도메인 모델
 */
data class Member(
    val id: Long? = null,
    val username: String,
    val email: String,
    val name: String,
    val cashpointBalance: BigDecimal = BigDecimal.ZERO,
    val status: MemberStatus = MemberStatus.ACTIVE,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    /**
     * 캐시포인트 차감 가능 여부 확인
     */
    fun canDeductCashpoint(amount: BigDecimal): Boolean {
        return status == MemberStatus.ACTIVE && 
               cashpointBalance >= amount && 
               amount > BigDecimal.ZERO
    }
    
    /**
     * 캐시포인트 차감
     */
    fun deductCashpoint(amount: BigDecimal): Member {
        require(canDeductCashpoint(amount)) { "캐시포인트 차감이 불가능합니다." }
        return copy(cashpointBalance = cashpointBalance - amount)
    }
    
    /**
     * 캐시포인트 충전
     */
    fun addCashpoint(amount: BigDecimal): Member {
        require(amount > BigDecimal.ZERO) { "충전 금액은 0보다 커야 합니다." }
        return copy(cashpointBalance = cashpointBalance + amount)
    }
}

/**
 * 회원 상태
 */
enum class MemberStatus {
    ACTIVE,     // 활성
    INACTIVE,   // 비활성
    SUSPENDED   // 정지
}
