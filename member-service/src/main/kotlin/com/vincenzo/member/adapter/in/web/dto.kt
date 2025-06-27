package com.vincenzo.member.adapter.`in`.web

import com.vincenzo.member.domain.model.Member
import com.vincenzo.member.domain.model.MemberStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 회원 응답 DTO
 */
data class MemberResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val cashpointBalance: BigDecimal,
    val status: MemberStatus,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id!!,
                username = member.username,
                email = member.email,
                name = member.name,
                cashpointBalance = member.cashpointBalance,
                status = member.status,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt
            )
        }
    }
}

/**
 * 캐시포인트 잔액 응답 DTO
 */
data class CashpointBalanceResponse(
    val balance: BigDecimal
)

/**
 * 캐시포인트 요청 DTO
 */
data class CashpointRequest(
    val amount: BigDecimal,
    val transactionId: String
)

/**
 * 캐시포인트 응답 DTO
 */
data class CashpointResponse(
    val success: Boolean,
    val message: String,
    val balance: BigDecimal?
) {
    companion object {
        fun success(balance: BigDecimal, message: String): CashpointResponse {
            return CashpointResponse(true, message, balance)
        }
        
        fun failure(message: String): CashpointResponse {
            return CashpointResponse(false, message, null)
        }
    }
}
