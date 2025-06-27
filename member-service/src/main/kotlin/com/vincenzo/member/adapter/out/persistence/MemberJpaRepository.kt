package com.vincenzo.member.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface MemberJpaRepository : JpaRepository<MemberEntity, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberEntity m WHERE m.id = :id")
    fun findByIdWithLock(@Param("id") id: Long): MemberEntity?
    
    @Modifying
    @Query("UPDATE MemberEntity m SET m.cashpointBalance = :balance, m.updatedAt = CURRENT_TIMESTAMP WHERE m.id = :id")
    fun updateCashpointBalance(@Param("id") id: Long, @Param("balance") balance: BigDecimal): Int
}
