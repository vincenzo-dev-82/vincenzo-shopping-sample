package com.vincenzo.member.application.service

import com.vincenzo.member.application.port.out.EventPublisher
import com.vincenzo.member.application.port.out.MemberRepository
import com.vincenzo.member.domain.model.Member
import com.vincenzo.member.domain.model.MemberStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class MemberServiceTest {
    
    private val memberRepository = mockk<MemberRepository>()
    private val eventPublisher = mockk<EventPublisher>(relaxed = true)
    private val memberService = MemberService(memberRepository, eventPublisher)
    
    @Test
    fun `회원 조회 성공`() {
        // Given
        val memberId = 1L
        val expectedMember = createTestMember(memberId)
        every { memberRepository.findById(memberId) } returns expectedMember
        
        // When
        val result = memberService.getMember(memberId)
        
        // Then
        assertThat(result).isEqualTo(expectedMember)
    }
    
    @Test
    fun `존재하지 않는 회원 조회시 null 반환`() {
        // Given
        val memberId = 999L
        every { memberRepository.findById(memberId) } returns null
        
        // When
        val result = memberService.getMember(memberId)
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `캐시포인트 차감 성공`() {
        // Given
        val memberId = 1L
        val deductAmount = BigDecimal("10000")
        val transactionId = "TXN123"
        val member = createTestMember(memberId, BigDecimal("50000"))
        
        every { memberRepository.findById(memberId) } returns member
        every { memberRepository.updateCashpointWithLock(memberId, any()) } returns true
        
        // When
        val result = memberService.deductCashpoint(memberId, deductAmount, transactionId)
        
        // Then
        assertThat(result.cashpointBalance).isEqualTo(BigDecimal("40000"))
        verify { eventPublisher.publishCashpointEvent(any()) }
    }
    
    @Test
    fun `캐시포인트 부족시 차감 실패`() {
        // Given
        val memberId = 1L
        val deductAmount = BigDecimal("60000")
        val transactionId = "TXN123"
        val member = createTestMember(memberId, BigDecimal("50000"))
        
        every { memberRepository.findById(memberId) } returns member
        
        // When & Then
        assertThrows<IllegalStateException> {
            memberService.deductCashpoint(memberId, deductAmount, transactionId)
        }
    }
    
    @Test
    fun `캐시포인트 환불 성공`() {
        // Given
        val memberId = 1L
        val refundAmount = BigDecimal("10000")
        val transactionId = "TXN123"
        val member = createTestMember(memberId, BigDecimal("50000"))
        
        every { memberRepository.findById(memberId) } returns member
        every { memberRepository.updateCashpointWithLock(memberId, any()) } returns true
        
        // When
        val result = memberService.refundCashpoint(memberId, refundAmount, transactionId)
        
        // Then
        assertThat(result.cashpointBalance).isEqualTo(BigDecimal("60000"))
        verify { eventPublisher.publishCashpointEvent(any()) }
    }
    
    private fun createTestMember(
        id: Long, 
        cashpointBalance: BigDecimal = BigDecimal("50000")
    ): Member {
        return Member(
            id = id,
            username = "test$id",
            email = "test$id@example.com",
            name = "테스트 사용자$id",
            cashpointBalance = cashpointBalance,
            status = MemberStatus.ACTIVE
        )
    }
}
