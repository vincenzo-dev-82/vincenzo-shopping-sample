package com.vincenzo.member.adapter.out.persistence

import com.vincenzo.member.application.port.out.MemberRepository
import com.vincenzo.member.domain.model.Member
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class MemberRepositoryAdapter(
    private val memberJpaRepository: MemberJpaRepository
) : MemberRepository {
    
    override fun findById(memberId: Long): Member? {
        return memberJpaRepository.findById(memberId)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun save(member: Member): Member {
        val entity = if (member.id != null) {
            memberJpaRepository.findById(member.id)
                .map { existing ->
                    existing.cashpointBalance = member.cashpointBalance
                    existing
                }
                .orElse(MemberEntity.fromDomain(member))
        } else {
            MemberEntity.fromDomain(member)
        }
        
        return memberJpaRepository.save(entity).toDomain()
    }
    
    override fun updateCashpointWithLock(memberId: Long, newBalance: BigDecimal): Boolean {
        return try {
            val updatedRows = memberJpaRepository.updateCashpointBalance(memberId, newBalance)
            updatedRows > 0
        } catch (e: Exception) {
            false
        }
    }
}
