package com.vincenzo.member.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MemberTest {

    @Test
    fun `캐시포인트 차감이 가능한 경우`() {
        // Given
        val member = Member(
            id = 1L,
            username = "test",
            email = "test@example.com",
            name = "테스트 사용자",
            cashpointBalance = BigDecimal.valueOf(10000),
            status = MemberStatus.ACTIVE
        )
        val deductAmount = BigDecimal.valueOf(5000)
        
        // When
        val result = member.canDeductCashpoint(deductAmount)
        
        // Then
        assertThat(result).isTrue()
    }
    
    @Test
    fun `캐시포인트 잔액이 부족한 경우`() {
        // Given
        val member = Member(
            id = 1L,
            username = "test",
            email = "test@example.com",
            name = "테스트 사용자",
            cashpointBalance = BigDecimal.valueOf(1000),
            status = MemberStatus.ACTIVE
        )
        val deductAmount = BigDecimal.valueOf(5000)
        
        // When
        val result = member.canDeductCashpoint(deductAmount)
        
        // Then
        assertThat(result).isFalse()
    }
    
    @Test
    fun `캐시포인트 차감 성공`() {
        // Given
        val member = Member(
            id = 1L,
            username = "test",
            email = "test@example.com",
            name = "테스트 사용자",
            cashpointBalance = BigDecimal.valueOf(10000),
            status = MemberStatus.ACTIVE
        )
        val deductAmount = BigDecimal.valueOf(3000)
        
        // When
        val result = member.deductCashpoint(deductAmount)
        
        // Then
        assertThat(result.cashpointBalance).isEqualTo(BigDecimal.valueOf(7000))
    }
    
    @Test
    fun `캐시포인트 차감 실패시 예외 발생`() {
        // Given
        val member = Member(
            id = 1L,
            username = "test",
            email = "test@example.com",
            name = "테스트 사용자",
            cashpointBalance = BigDecimal.valueOf(1000),
            status = MemberStatus.ACTIVE
        )
        val deductAmount = BigDecimal.valueOf(5000)
        
        // When & Then
        assertThatThrownBy {
            member.deductCashpoint(deductAmount)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("캐시포인트 차감이 불가능합니다.")
    }
}
