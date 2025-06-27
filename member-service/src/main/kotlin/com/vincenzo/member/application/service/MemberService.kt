package com.vincenzo.member.application.service

import com.vincenzo.member.application.port.`in`.MemberUseCase
import com.vincenzo.member.application.port.out.EventPublisher
import com.vincenzo.member.application.port.out.MemberRepository
import com.vincenzo.member.domain.event.CashpointDeductedEvent
import com.vincenzo.member.domain.event.CashpointRefundedEvent
import com.vincenzo.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) : MemberUseCase {
    
    @Transactional(readOnly = true)
    override fun getMember(memberId: Long): Member? {
        return memberRepository.findById(memberId)
    }
    
    @Transactional(readOnly = true)
    override fun getCashpointBalance(memberId: Long): BigDecimal {
        val member = memberRepository.findById(memberId)
            ?: throw IllegalArgumentException("존재하지 않는 회원입니다. ID: $memberId")
        return member.cashpointBalance
    }
    
    override fun deductCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Member {
        val member = memberRepository.findById(memberId)
            ?: throw IllegalArgumentException("존재하지 않는 회원입니다. ID: $memberId")
        
        if (!member.canDeductCashpoint(amount)) {
            throw IllegalStateException("캐시포인트가 부족합니다. 요청: $amount, 잔액: ${member.cashpointBalance}")
        }
        
        val updatedMember = member.deductCashpoint(amount)
        
        // 동시성 제어를 위한 업데이트
        val success = memberRepository.updateCashpointWithLock(memberId, updatedMember.cashpointBalance)
        if (!success) {
            throw IllegalStateException("동시 업데이트로 인한 실패")
        }
        
        // 이벤트 발행
        val event = CashpointDeductedEvent(
            memberId = memberId,
            amount = amount,
            transactionId = transactionId,
            remainingBalance = updatedMember.cashpointBalance
        )
        eventPublisher.publishCashpointEvent(event)
        
        return updatedMember
    }
    
    override fun refundCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Member {
        val member = memberRepository.findById(memberId)
            ?: throw IllegalArgumentException("존재하지 않는 회원입니다. ID: $memberId")
        
        val updatedMember = member.addCashpoint(amount)
        
        // 동시성 제어를 위한 업데이트
        val success = memberRepository.updateCashpointWithLock(memberId, updatedMember.cashpointBalance)
        if (!success) {
            throw IllegalStateException("동시 업데이트로 인한 실패")
        }
        
        // 이벤트 발행
        val event = CashpointRefundedEvent(
            memberId = memberId,
            amount = amount,
            transactionId = transactionId,
            newBalance = updatedMember.cashpointBalance
        )
        eventPublisher.publishCashpointEvent(event)
        
        return updatedMember
    }
}
