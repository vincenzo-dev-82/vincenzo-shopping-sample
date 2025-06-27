package com.vincenzo.member.adapter.out.persistence

import com.vincenzo.member.domain.model.Member
import com.vincenzo.member.domain.model.MemberStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class MemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 50)
    val username: String,
    
    @Column(unique = true, nullable = false, length = 100)
    val email: String,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(name = "cashpoint_balance", precision = 12, scale = 2)
    var cashpointBalance: BigDecimal = BigDecimal.ZERO,
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: MemberStatus = MemberStatus.ACTIVE,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            username = username,
            email = email,
            name = name,
            cashpointBalance = cashpointBalance,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(member: Member): MemberEntity {
            return MemberEntity(
                id = member.id,
                username = member.username,
                email = member.email,
                name = member.name,
                cashpointBalance = member.cashpointBalance,
                status = member.status,
                createdAt = member.createdAt ?: LocalDateTime.now(),
                updatedAt = member.updatedAt ?: LocalDateTime.now()
            )
        }
    }
}
