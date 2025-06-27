package com.vincenzo.payment.adapter.out.persistence

import com.vincenzo.payment.domain.model.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "payment_key", unique = true, nullable = false, length = 100)
    val paymentKey: String,
    
    @Column(name = "order_id", nullable = false)
    val orderId: Long,
    
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    val paymentType: PaymentType,
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val paymentMethods: MutableList<PaymentMethodEntity> = mutableListOf(),
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Payment {
        return Payment(
            id = id,
            paymentKey = paymentKey,
            orderId = orderId,
            totalAmount = totalAmount,
            paymentType = paymentType,
            paymentMethods = paymentMethods.map { it.toDomain() },
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(payment: Payment): PaymentEntity {
            val entity = PaymentEntity(
                id = payment.id,
                paymentKey = payment.paymentKey,
                orderId = payment.orderId,
                totalAmount = payment.totalAmount,
                paymentType = payment.paymentType,
                status = payment.status,
                createdAt = payment.createdAt ?: LocalDateTime.now(),
                updatedAt = payment.updatedAt ?: LocalDateTime.now()
            )
            
            payment.paymentMethods.forEach { paymentMethod ->
                val methodEntity = PaymentMethodEntity.fromDomain(paymentMethod, entity)
                entity.paymentMethods.add(methodEntity)
            }
            
            return entity
        }
    }
}

@Entity
@Table(name = "payment_methods")
class PaymentMethodEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    val payment: PaymentEntity,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 20)
    val methodType: PaymentMethodType,
    
    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val status: PaymentMethodStatus = PaymentMethodStatus.PENDING,
    
    @Column(name = "external_transaction_id", length = 100)
    val externalTransactionId: String? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): PaymentMethod {
        return PaymentMethod(
            id = id,
            methodType = methodType,
            amount = amount,
            status = status,
            externalTransactionId = externalTransactionId,
            additionalInfo = emptyMap() // JPA에서는 간단히 처리
        )
    }
    
    companion object {
        fun fromDomain(paymentMethod: PaymentMethod, paymentEntity: PaymentEntity): PaymentMethodEntity {
            return PaymentMethodEntity(
                id = paymentMethod.id,
                payment = paymentEntity,
                methodType = paymentMethod.methodType,
                amount = paymentMethod.amount,
                status = paymentMethod.status,
                externalTransactionId = paymentMethod.externalTransactionId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}
