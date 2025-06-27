package com.vincenzo.order.application.port.out

import java.math.BigDecimal

/**
 * 회원 서비스 클라이언트 인터페이스
 */
interface MemberServiceClient {
    /**
     * 회원 조회
     */
    suspend fun getMember(memberId: Long): MemberInfo?
    
    /**
     * 캐시포인트 잔액 조회
     */
    suspend fun getCashpointBalance(memberId: Long): BigDecimal?
}

/**
 * 회원 정보
 */
data class MemberInfo(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val cashpointBalance: BigDecimal,
    val status: String
)
