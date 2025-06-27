package com.vincenzo.payment.adapter.out.persistence

import com.vincenzo.payment.application.port.out.PaymentRepository
import com.vincenzo.payment.domain.model.Payment
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryAdapter(
    private val paymentJpaRepository: PaymentJpaRepository
) : PaymentRepository {
    
    override fun save(payment: Payment): Payment {
        val entity = if (payment.id != null) {
            // 업데이트
            val existing = paymentJpaRepository.findByPaymentKeyWithMethods(payment.paymentKey)
            if (existing != null) {
                // 기존 엔티티의 상태만 업데이트
                val updated = PaymentEntity(
                    id = existing.id,
                    paymentKey = existing.paymentKey,
                    orderId = existing.orderId,
                    totalAmount = existing.totalAmount,
                    paymentType = existing.paymentType,
                    status = payment.status,
                    createdAt = existing.createdAt,
                    updatedAt = java.time.LocalDateTime.now()
                )
                
                // 기존 결제 방법들 업데이트
                payment.paymentMethods.forEachIndexed { index, paymentMethod ->
                    if (index < existing.paymentMethods.size) {
                        val existingMethod = existing.paymentMethods[index]
                        val updatedMethod = PaymentMethodEntity(
                            id = existingMethod.id,
                            payment = updated,
                            methodType = existingMethod.methodType,
                            amount = existingMethod.amount,
                            status = paymentMethod.status,
                            externalTransactionId = paymentMethod.externalTransactionId ?: existingMethod.externalTransactionId,
                            createdAt = existingMethod.createdAt,
                            updatedAt = java.time.LocalDateTime.now()
                        )
                        updated.paymentMethods.add(updatedMethod)
                    }
                }
                updated
            } else {
                PaymentEntity.fromDomain(payment)
            }
        } else {
            PaymentEntity.fromDomain(payment)
        }
        
        return paymentJpaRepository.save(entity).toDomain()
    }
    
    override fun findByPaymentKey(paymentKey: String): Payment? {
        return paymentJpaRepository.findByPaymentKeyWithMethods(paymentKey)?.toDomain()
    }
    
    override fun findByOrderId(orderId: Long): Payment? {
        return paymentJpaRepository.findByOrderIdWithMethods(orderId)?.toDomain()
    }
}
