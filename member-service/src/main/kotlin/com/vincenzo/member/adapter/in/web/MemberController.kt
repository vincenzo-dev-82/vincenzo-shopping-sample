package com.vincenzo.member.adapter.`in`.web

import com.vincenzo.member.application.port.`in`.MemberUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Member", description = "회원 관리 API")
class MemberController(
    private val memberUseCase: MemberUseCase
) {
    
    @GetMapping("/{memberId}")
    @Operation(summary = "회원 조회", description = "회원 정보를 조회합니다.")
    fun getMember(@PathVariable memberId: Long): ResponseEntity<MemberResponse> {
        val member = memberUseCase.getMember(memberId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(MemberResponse.fromDomain(member))
    }
    
    @GetMapping("/{memberId}/cashpoint")
    @Operation(summary = "캐시포인트 조회", description = "회원의 캐시포인트 잔액을 조회합니다.")
    fun getCashpointBalance(@PathVariable memberId: Long): ResponseEntity<CashpointBalanceResponse> {
        return try {
            val balance = memberUseCase.getCashpointBalance(memberId)
            ResponseEntity.ok(CashpointBalanceResponse(balance))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{memberId}/cashpoint/deduct")
    @Operation(summary = "캐시포인트 차감", description = "회원의 캐시포인트를 차감합니다.")
    fun deductCashpoint(
        @PathVariable memberId: Long,
        @RequestBody request: CashpointRequest
    ): ResponseEntity<CashpointResponse> {
        return try {
            val updatedMember = memberUseCase.deductCashpoint(memberId, request.amount, request.transactionId)
            ResponseEntity.ok(CashpointResponse.success(updatedMember.cashpointBalance, "차감 성공"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(CashpointResponse.failure(e.message ?: "차감 실패"))
        }
    }
    
    @PostMapping("/{memberId}/cashpoint/refund")
    @Operation(summary = "캐시포인트 환불", description = "회원에게 캐시포인트를 환불합니다.")
    fun refundCashpoint(
        @PathVariable memberId: Long,
        @RequestBody request: CashpointRequest
    ): ResponseEntity<CashpointResponse> {
        return try {
            val updatedMember = memberUseCase.refundCashpoint(memberId, request.amount, request.transactionId)
            ResponseEntity.ok(CashpointResponse.success(updatedMember.cashpointBalance, "환불 성공"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(CashpointResponse.failure(e.message ?: "환불 실패"))
        }
    }
}
