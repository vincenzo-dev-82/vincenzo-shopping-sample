package com.vincenzo.member.adapter.`in`.grpc

import com.vincenzo.grpc.member.*
import com.vincenzo.member.application.port.`in`.MemberUseCase
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import java.math.BigDecimal

@GrpcService
class MemberGrpcService(
    private val memberUseCase: MemberUseCase
) : MemberServiceGrpcKt.MemberServiceCoroutineImplBase() {
    
    override suspend fun getMember(request: GetMemberRequest): GetMemberResponse {
        return try {
            val member = memberUseCase.getMember(request.memberId)
            if (member != null) {
                GetMemberResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("성공")
                    .setMember(
                        Member.newBuilder()
                            .setId(member.id!!)
                            .setUsername(member.username)
                            .setEmail(member.email)
                            .setName(member.name)
                            .setCashpointBalance(member.cashpointBalance.toDouble())
                            .setStatus(member.status.name)
                            .build()
                    )
                    .build()
            } else {
                GetMemberResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("회원을 찾을 수 없습니다.")
                    .build()
            }
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun getCashPoint(request: GetCashPointRequest): GetCashPointResponse {
        return try {
            val balance = memberUseCase.getCashpointBalance(request.memberId)
            GetCashPointResponse.newBuilder()
                .setSuccess(true)
                .setMessage("성공")
                .setBalance(balance.toDouble())
                .build()
        } catch (e: IllegalArgumentException) {
            GetCashPointResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.message ?: "회원을 찾을 수 없습니다.")
                .setBalance(0.0)
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun deductCashPoint(request: DeductCashPointRequest): DeductCashPointResponse {
        return try {
            val updatedMember = memberUseCase.deductCashpoint(
                request.memberId,
                BigDecimal.valueOf(request.amount),
                request.transactionId
            )
            DeductCashPointResponse.newBuilder()
                .setSuccess(true)
                .setMessage("차감 성공")
                .setRemainingBalance(updatedMember.cashpointBalance.toDouble())
                .build()
        } catch (e: IllegalStateException) {
            DeductCashPointResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.message ?: "차감 실패")
                .setRemainingBalance(0.0)
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
    
    override suspend fun refundCashPoint(request: RefundCashPointRequest): RefundCashPointResponse {
        return try {
            val updatedMember = memberUseCase.refundCashpoint(
                request.memberId,
                BigDecimal.valueOf(request.amount),
                request.transactionId
            )
            RefundCashPointResponse.newBuilder()
                .setSuccess(true)
                .setMessage("환불 성공")
                .setNewBalance(updatedMember.cashpointBalance.toDouble())
                .build()
        } catch (e: Exception) {
            throw StatusException(Status.INTERNAL.withDescription(e.message))
        }
    }
}
